package syncer.syncerservice.util.queue.internal;



import syncer.syncerservice.exception.FileFormatException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据索引文件
 *
 * @author HeDYn <a href='mailto:hedyn@foxmail.com'>hedyn</a>
 * @author sunli
 */
public class Index {
    private static final int INDEX_LIMIT_LENGTH = 32;
    private static final String INDEX_FILE_NAME = "fq.idx";

    private RandomAccessFile dbRandFile;
    private FileChannel fc;
    private MappedByteBuffer mappedByteBuffer;

    /**
     * 文件操作位置信息
     */
    private String magicString = null;
    private int version = -1;
    private int readerPosition = -1;
    private int writerPosition = -1;
    private int readerIndex = -1;
    private int writerIndex = -1;
    private AtomicInteger size = new AtomicInteger();

    public Index(String path) throws IOException, FileFormatException {
        File dbFile = new File(path, INDEX_FILE_NAME);

        // 文件不存在，创建文件
        if (dbFile.exists() == false) {
            dbFile.createNewFile();
            dbRandFile = new RandomAccessFile(dbFile, "rwd");
            initIdxFile();
        } else {
            dbRandFile = new RandomAccessFile(dbFile, "rwd");
            if (dbRandFile.length() < INDEX_LIMIT_LENGTH) {
                throw new FileFormatException("file format error.");
            }
            byte[] bytes = new byte[INDEX_LIMIT_LENGTH];
            dbRandFile.read(bytes);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            bytes = new byte[Entity.MAGIC.getBytes().length];
            buffer.get(bytes);
            magicString = new String(bytes);
            version = buffer.getInt();
            readerPosition = buffer.getInt();
            writerPosition = buffer.getInt();
            readerIndex = buffer.getInt();
            writerIndex = buffer.getInt();
            int sz = buffer.getInt();
            if (readerPosition == writerPosition && readerIndex == writerIndex && sz <= 0) {
                initIdxFile();
            } else {
                size.set(sz);
            }
        }
        fc = dbRandFile.getChannel();
        mappedByteBuffer = fc.map(MapMode.READ_WRITE, 0, INDEX_LIMIT_LENGTH);
    }

    private void initIdxFile() throws IOException {
        magicString = Entity.MAGIC;
        version = 1;
        readerPosition = Entity.MESSAGE_START_POSITION;
        writerPosition = Entity.MESSAGE_START_POSITION;
        readerIndex = 1;
        writerIndex = 1;
        dbRandFile.setLength(32);
        dbRandFile.seek(0);
        dbRandFile.write(magicString.getBytes());// magic
        dbRandFile.writeInt(version);// 8 version
        dbRandFile.writeInt(readerPosition);// 12 reader position
        dbRandFile.writeInt(writerPosition);// 16 write position
        dbRandFile.writeInt(readerIndex);// 20 reader index
        dbRandFile.writeInt(writerIndex);// 24 writer index
        dbRandFile.writeInt(0);// 28 size
    }

    public void clear() throws IOException {
        mappedByteBuffer.clear();
        mappedByteBuffer.force();
        initIdxFile();
    }

    /**
     * 记录写位置
     *
     * @param pos
     */
    public void putWriterPosition(int pos) {
        mappedByteBuffer.position(16);
        mappedByteBuffer.putInt(pos);
        this.writerPosition = pos;
    }

    /**
     * 记录读取的位置
     *
     * @param pos
     */
    public void putReaderPosition(int pos) {
        mappedByteBuffer.position(12);
        mappedByteBuffer.putInt(pos);
        this.readerPosition = pos;
    }

    /**
     * 记录写文件索引
     *
     * @param index
     */
    public void putWriterIndex(int index) {
        mappedByteBuffer.position(24);
        mappedByteBuffer.putInt(index);
        this.writerIndex = index;
    }

    /**
     * 记录读取文件索引
     *
     * @param index
     */
    public void putReaderIndex(int index) {
        mappedByteBuffer.position(20);
        mappedByteBuffer.putInt(index);
        this.readerIndex = index;
    }

    public void incrementSize() {
        int num = size.incrementAndGet();
        mappedByteBuffer.position(28);
        mappedByteBuffer.putInt(num);
    }

    public void decrementSize() {
        int num = size.decrementAndGet();
        mappedByteBuffer.position(28);
        mappedByteBuffer.putInt(num);
    }

    public String getMagicString() {
        return magicString;
    }

    public int getVersion() {
        return version;
    }

    public int getReaderPosition() {
        return readerPosition;
    }

    public int getWriterPosition() {
        return writerPosition;
    }

    public int getReaderIndex() {
        return readerIndex;
    }

    public int getWriterIndex() {
        return writerIndex;
    }

    public int getSize() {
        return size.get();
    }

    /**
     * 关闭索引文件
     */
    public void close() throws IOException {
        mappedByteBuffer.force();
        mappedByteBuffer.clear();
        fc.close();
        dbRandFile.close();
        mappedByteBuffer = null;
        fc = null;
        dbRandFile = null;
    }

    public String headerInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(" magicString:");
        sb.append(magicString);
        sb.append(" version:");
        sb.append(version);
        sb.append(" readerPosition:");
        sb.append(readerPosition);
        sb.append(" writerPosition:");
        sb.append(writerPosition);
        sb.append(" size:");
        sb.append(size);
        sb.append(" readerIndex:");
        sb.append(readerIndex);
        sb.append(" writerIndex:");
        sb.append(writerIndex);
        return sb.toString();
    }

}
