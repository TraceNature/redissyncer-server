package syncer.replica.io;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.util.bytes.ByteArray;
import syncer.replica.util.strings.Strings;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 输入流
 * @author: Eq Zhan
 * @create: 2021-03-15
 **/
@Slf4j
public class RedisInputStream1 extends InputStream {
    /**
     * 记录头
     */
    private int head;
    /**
     * 记录尾
     */
    private int tail;

    private long total;

    private InputStream in;

    private byte[] buffer;

    /**
     * 标记 用于计算offset
     */
    private boolean mark=false;

    /**
     * 用于计算offset
     */
    private long markLength;




    public RedisInputStream1(InputStream in, int bufferLen) {
        this.in = in;
        this.buffer=new byte[bufferLen];
    }

    /**
     * 8*1024
     * @param in
     */
    public RedisInputStream1(InputStream in) {
        this(in,8192);
    }



    /**
     * 标记
     */
    public void mark(){
        if(!mark){
            mark=true;
            return;
        }
        throw new AssertionError("already marked");
    }

    public void mark(long len) {
        mark();
        markLength = len;
    }

    public boolean isMarked() {
        return mark;
    }

    /**
     * 取消标志并获取长度
     * @return
     */
    public long unmark(){
        if(mark){
            long rsize=markLength;
            markLength=0;
            mark=false;
            return rsize;
        }
        throw new AssertionError("must mark first");
    }


    /**
     *
     * @param length
     * @param littleEndian 小端
     * @return
     * 1.小端法(Little - Endian)就是低位字节排放在内存的低地址端即该值的起始地址，高位字节排放在内存的高地址端。
     * 2.大端法(Big-Endian)就是高位字节排放在内存的低地址端即该值的起始地址，低位字节排放在内存的高地址端。
     * https://blog.csdn.net/weixin_39764212/article/details/114517003
     * @throws IOException
     */
    public int readInt(int length, boolean littleEndian) throws IOException{
        int r=0;
        for (int i=0;i<length;i++){
            final int value=this.read();
            //小端模式
            if(littleEndian){
                r |= (value << (i << 3));
            }else {
                r = (r << 8) | value;
            }
        }
        int c;
        return r << (c = (4 - length << 3)) >> c;
    }

    public long readUInt(int length) throws IOException {
        return readUInt(length, true);
    }


    public long readUInt(int length, boolean littleEndian) throws IOException {
        return readInt(length, littleEndian) & 0xFFFFFFFFL;
    }


    public int readInt(byte[] bytes) {
        return readInt(bytes, true);
    }

    public int readInt(byte[] bytes, boolean littleEndian) {
        int r = 0;
        int length = bytes.length;
        for (int i = 0; i < length; ++i) {
            final int v = bytes[i] & 0xFF;
            if (littleEndian) {
                r |= (v << (i << 3));
            } else {
                r = (r << 8) | v;
            }
        }
        int c;
        return r << (c = (4 - length << 3)) >> c;
    }


    public long readLong(int length, boolean littleEndian) throws IOException {
        long r = 0;
        for (int i = 0; i < length; ++i) {
            final long v = this.read();
            if (littleEndian) {
                r |= (v << (i << 3));
            } else {
                r = (r << 8) | v;
            }
        }
        return r;
    }

    public String readString(int len) throws IOException {
        return Strings.toString(readBytes(len).first());
    }

    public String readString(int len, Charset charset) throws IOException {
        return Strings.toString(readBytes(len).first(), charset);
    }




    /**
     * 读取byte[]
     * @param len
     * @return
     * @throws IOException
     */
    public ByteArray readBytes(long len) throws IOException {
        ByteArray bytes = new ByteArray(len);
        this.read(bytes, 0, len);
        if (mark) markLength += len;
        return bytes;
    }



    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return (int) read(new ByteArray(b), off, len);
    }

    /**
     * 获取可用空间
     * @return
     * @throws IOException
     */
    @Override
    public int available() throws IOException {
        return tail - head + in.available();
    }


    /**
     * & 0xff   https://www.cnblogs.com/yangyuqing/p/12408405.html
     * byte[]->int 保持二进制补码的一致性
     * @return
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        if(head>tail){
            fill();
        }
        //标记
        if(mark){
            markLength+=1;
        }
        byte data=buffer[head++];
        notify(data);
        return data & 0xff;
    }




    /**
     *
     * @param bytes
     * @param offset
     * @param len
     * @return
     * @throws IOException
     */
    public long read(ByteArray bytes, long offset, long len) throws IOException{
        long total = len;
        long index = offset;
        while (total>0){
            //可用
            int available = tail - head;
            if (available >= total) {
                ByteArray.arraycopy(new ByteArray(buffer), head, bytes, index, total);
                head += total;
                break;
            } else {
                ByteArray.arraycopy(new ByteArray(buffer), head, bytes, index, available);
                index += available;
                total -= available;
                fill();
            }
        }

        for (byte[] b : bytes) {
            notify(b);
        }
        return len;
    }

    /**
     *
     * @param data
     */
    private void notify(byte... data) {
    }

    /**
     * 读取buffer长度
     * @throws IOException
     */
    private void fill() throws IOException {
        tail = in.read(buffer, 0, buffer.length);
        if (tail == -1) throw new EOFException("end of file or end of stream.");
        total += tail;
        head = 0;
    }


    /**
     * 跳过流中的多个字节
     * @param len
     * @param notify
     * @return
     * @throws IOException
     */
    public long skip(long len, boolean notify) throws IOException {
        long total = len;
        while (total > 0) {
            int available = tail - head;
            if (available >= total) {
                if (notify) notify(Arrays.copyOfRange(buffer, head, head + (int) total));
                head += total;
                break;
            } else {
                if (notify) notify(Arrays.copyOfRange(buffer, head, tail));
                total -= available;
                fill();
            }
        }
        return len;
    }

    @Override
    public long skip(long len) throws IOException {
        return skip(len, true);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }


    public int readInt(int len) throws IOException {
        return readInt(len, true);
    }

    public long readLong(int len) throws IOException {
        return readLong(len, true);
    }


}
