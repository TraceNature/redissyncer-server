package syncer.syncerservice.util.queue;


import syncer.syncerservice.exception.FileEOFException;
import syncer.syncerservice.exception.FileFormatException;
import syncer.syncerservice.util.queue.internal.Entity;
import syncer.syncerservice.util.queue.internal.Index;

import java.io.File;
import java.io.IOException;

/**
 * 基于文件的先进先出的读写队列
 *
 * @author HeDYn <a href='mailto:hedyn@foxmail.com'>hedyn</a>
 * @author sunli
 */
public class FSQueue {
    private int entityLimitLength;
    private String path = null;
    /**
     * 文件操作实例
     */
    private Index idx = null;
    private Entity writerHandle = null;
    private Entity readerHandle = null;
    /**
     * 文件操作位置信息
     */
    private int readerIndex = -1;
    private int writerIndex = -1;

    public FSQueue(String dir) throws IOException, FileFormatException {
        this(new File(dir));
    }

    /**
     * 在指定的目录中，以fileLimitLength为单个数据文件的最大大小限制初始化队列存储
     *
     * @param dir               队列数据存储的路径
     * @param entityLimitLength 单个数据文件的大小，不能超过2G
     * @throws IOException
     * @throws FileFormatException
     */
    public FSQueue(String dir, int entityLimitLength) throws IOException, FileFormatException {
        this(new File(dir), entityLimitLength);
    }

    public FSQueue(File dir) throws IOException, FileFormatException {
        this(dir, 1024 * 1024 * 2);
    }

    /**
     * 在指定的目录中，以fileLimitLength为单个数据文件的最大大小限制初始化队列存储
     *
     * @param dir               队列数据存储的目录
     * @param entityLimitLength 单个数据文件的大小，不能超过2G
     * @throws IOException
     * @throws FileFormatException
     */
    public FSQueue(File dir, int entityLimitLength) throws IOException, FileFormatException {
        if (dir.exists() == false && dir.isDirectory() == false) {
            if (dir.mkdirs() == false) {
                throw new IOException("create dir error");
            }
        }
        this.entityLimitLength = entityLimitLength;
        path = dir.getAbsolutePath();
        // 打开索引文件
        idx = new Index(path);
        initHandle();
    }

    private void initHandle() throws IOException, FileFormatException {
        writerIndex = idx.getWriterIndex();
        readerIndex = idx.getReaderIndex();
        writerHandle = new Entity(path, writerIndex, entityLimitLength, idx);
        if (readerIndex == writerIndex) {
            readerHandle = writerHandle;
        } else {
            readerHandle = new Entity(path, readerIndex, entityLimitLength, idx);
        }
    }

    /**
     * 一个文件的数据写入达到fileLimitLength的时候，滚动到下一个文件实例
     *
     * @throws IOException
     * @throws FileFormatException
     */
    private void rotateNextLogWriter() throws IOException, FileFormatException {
        writerIndex = writerIndex + 1;
        writerHandle.putNextFileNumber(writerIndex);
        if (readerHandle != writerHandle) {
            writerHandle.close();
        }
        idx.putWriterIndex(writerIndex);
        writerHandle = new Entity(path, writerIndex, entityLimitLength, idx, true);
    }

    /**
     * 向队列存储添加一个字符串
     *
     * @param message message
     * @throws IOException
     * @throws FileFormatException
     */
    public void add(String message) throws IOException, FileFormatException {
        add(message.getBytes());
    }

    /**
     * 向队列存储添加一个byte数组
     *
     * @param message
     * @throws IOException
     * @throws FileFormatException
     */
    public void add(byte[] message) throws IOException, FileFormatException {
        short status = writerHandle.write(message);
        if (status == Entity.WRITEFULL) {
            rotateNextLogWriter();
            status = writerHandle.write(message);
        }
        if (status == Entity.WRITESUCCESS) {
            idx.incrementSize();
        }
    }

    private byte[] read(boolean commit) throws IOException, FileFormatException {
        byte[] bytes;
        try {
            bytes = readerHandle.read(commit);
        } catch (FileEOFException e) {
            int nextFileNumber = readerHandle.getNextFileNumber();
            readerHandle.reset();
            File deleteFile = readerHandle.getFile();
            readerHandle.close();
            deleteFile.delete();
            // 更新下一次读取的位置和索引
            idx.putReaderPosition(Entity.MESSAGE_START_POSITION);
            idx.putReaderIndex(nextFileNumber);
            if (writerHandle.getCurrentFileNumber() == nextFileNumber) {
                readerHandle = writerHandle;
            } else {
                readerHandle = new Entity(path, nextFileNumber, entityLimitLength, idx);
            }
            try {
                bytes = readerHandle.read(commit);
            } catch (FileEOFException e1) {
                throw new FileFormatException(e1);
            }
        }
        if (commit && bytes != null) {
            idx.decrementSize();
        }
        return bytes;
    }

    /**
     * 读取队列头的数据，但不移除。
     *
     * @return
     * @throws IOException
     * @throws FileFormatException
     */
    public byte[] readNext() throws IOException, FileFormatException {
        return read(false);
    }

    /**
     * 从队列存储中取出最先入队的数据，并移除它
     *
     * @return
     * @throws IOException
     * @throws FileFormatException
     */
    public byte[] readNextAndRemove() throws IOException, FileFormatException {
        return read(true);
    }

    public void clear() throws IOException, FileFormatException {
        idx.clear();
        initHandle();
    }

    public void close() throws IOException {
        readerHandle.close();
        writerHandle.close();
        idx.close();
    }

    public int getQueueSize() {
        return idx.getSize();
    }
}
