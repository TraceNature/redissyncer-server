package syncer.syncerreplication.io.stream;


import syncer.syncerreplication.io.RawByteListener;
import syncer.syncerreplication.util.objectUtils.ByteArray;
import syncer.syncerreplication.util.objectUtils.Strings;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public class RedisInputStream extends InputStream {
     int head = 0;
     int tail = 0;
     long total = 0;
     long markLen = 0;
     final byte[] buf;
     boolean mark = false;
     final InputStream in;
     List<RawByteListener> rawByteListeners;

    public RedisInputStream(ByteArray array) {
        this(new ByteArrayInputStream(array));
    }

    public RedisInputStream(final InputStream in) {
        this(in, 8192);
    }

    public RedisInputStream(final InputStream in, int len) {
        this.in = in;
        this.buf = new byte[len];
    }


    /**
     * @param rawByteListeners raw byte listeners
     * @since 2.2.0
     */
    public synchronized void setRawByteListeners(List<RawByteListener> rawByteListeners) {
        this.rawByteListeners = rawByteListeners;
    }

    protected void notify(byte... bytes) {
        if (rawByteListeners == null || rawByteListeners.isEmpty()) {
            return;
        }
        for (RawByteListener listener : rawByteListeners) {
            listener.handle(bytes);
        }
    }

    /**
     * 获取头
     * @return
     */
    public int head() {
        return head;
    }

    /**
     * 获取尾
     * @return
     */
    public int tail() {
        return tail;
    }

    /**
     * 获取缓冲区大小
     * @return
     */
    public int bufSize() {
        return buf.length;
    }

    /**
     * 是否标记
     * @return
     */
    public boolean isMarked() {
        return mark;
    }

    /**
     * 标记 并记录标记长度
     * @param len
     */
    public void mark(long len) {
        mark();
        markLen = len;
    }

    public void mark() {
        if (!mark) {
            mark = true;
            return;
        }
        throw new AssertionError("already marked");
    }

    /**
     * 取消标记
     * @return
     */
    public long unmark() {
        if (mark) {
            long rs = markLen;
            markLen = 0;
            mark = false;
            return rs;
        }
        throw new AssertionError("must mark first");
    }

    public long total() {
        return total;
    }

    /**
     * 读取bytes
     * @param len
     * @return
     * @throws IOException
     */
    public ByteArray readBytes(long len) throws IOException {
        ByteArray bytes = new ByteArray(len);
        this.read(bytes, 0, len);
        if (mark) {
            markLen += len;
        }
        return bytes;
    }

    /**
     * 读取int
     * @param len
     * @return
     * @throws IOException
     */
    public int readInt(int len) throws IOException {
        return readInt(len, true);
    }

    /**
     * 读取long
     * @param len
     * @return
     * @throws IOException
     */
    public long readLong(int len) throws IOException {
        return readLong(len, true);
    }

    public int readInt(int length, boolean littleEndian) throws IOException {
        int r = 0;
        for (int i = 0; i < length; ++i) {
            final int v = this.read();
            if (littleEndian) {
                r |= (v << (i << 3));
            } else {
                r = (r << 8) | v;
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

    @Override
    public int read() throws IOException {
        if (head >= tail){
            fill();
        }
        if (mark) {
            markLen += 1;
        }
        byte b = buf[head++];
        notify(b);
        return b & 0xff;
    }

    public long read(ByteArray bytes, long offset, long len) throws IOException {
        long total = len;
        long index = offset;
        while (total > 0) {
            int available = tail - head;
            if (available >= total) {
                ByteArray.arraycopy(new ByteArray(buf), head, bytes, index, total);
                head += total;
                break;
            } else {
                ByteArray.arraycopy(new ByteArray(buf), head, bytes, index, available);
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

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return (int) read(new ByteArray(b), off, len);
    }

    @Override
    public int available() throws IOException {
        return tail - head + in.available();
    }

    public long skip(long len, boolean notify) throws IOException {
        long total = len;
        while (total > 0) {
            int available = tail - head;
            if (available >= total) {
                if (notify) {
                    notify(Arrays.copyOfRange(buf, head, head + (int) total));
                }
                head += total;
                break;
            } else {
                if (notify){
                    notify(Arrays.copyOfRange(buf, head, tail));
                }
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

    protected void fill() throws IOException {
        tail = in.read(buf, 0, buf.length);
        if (tail == -1){
            throw new EOFException("end of file or end of stream.");
        }
        total += tail;
        head = 0;
    }
}
