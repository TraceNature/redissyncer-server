package syncer.syncerservice.util.queue;


import syncer.syncerservice.exception.FileFormatException;
import syncer.syncerservice.util.jedis.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于文件系统的持久化队列
 *
 * @author <a href=mailto:hedyn@foxmail.com>HeDYn</a>
 * @author sunli
 */
public class FQueue extends AbstractQueue<byte[]> {

    private FSQueue fsQueue = null;
    private Lock lock = new ReentrantReadWriteLock().writeLock();


    public FQueue(String path) throws IOException, FileFormatException {
        fsQueue = new FSQueue(path);
    }

    /**
     * 创建一个持久化队列
     *
     * @param path              文件的存储路径
     * @param entityLimitLength 存储数据的单个文件的大小
     * @throws IOException
     * @throws FileFormatException
     */
    public FQueue(String path, int entityLimitLength) throws IOException, FileFormatException {
        fsQueue = new FSQueue(path, entityLimitLength);
    }

    public FQueue(File dir) throws IOException, FileFormatException {
        fsQueue = new FSQueue(dir);
    }

    /**
     * 创建一个持久化队列
     *
     * @param dir               文件的存储目录
     * @param entityLimitLength 存储数据的单个文件的大小
     * @throws IOException
     * @throws FileFormatException
     */
    public FQueue(File dir, int entityLimitLength) throws IOException, FileFormatException {
        fsQueue = new FSQueue(dir, entityLimitLength);
    }

    @Override
    public Iterator<byte[]> iterator() {
        throw new UnsupportedOperationException("iterator Unsupported now");
    }

    @Override
    public int size() {
        return fsQueue.getQueueSize();
    }

    @Override
    public boolean offer(byte[] e) {
        lock.lock();
        try {
            fsQueue.add(e);
            return true;
        } catch (Exception ex) {
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public byte[] peek() {
        lock.lock();
        try {
            return fsQueue.readNext();
        } catch (IOException ex) {
            return null;
        } catch (FileFormatException ex) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public byte[] poll() {
        try {


            return fsQueue.readNextAndRemove();


        } catch (Exception ex) {
            return null;
        } finally {

        }

    }


    public byte[] take() throws InterruptedException {
        try {
            try {
                while (size() <= 0){
                    System.out.println("队列元素为空，进入阻塞...");
                }
                } catch (Exception ie) {
                System.out.println("出现异常，唤醒阻塞线程conditionNull");
                throw ie;
            }

            byte[]x = poll();
            return x;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("取出方法释放锁...");
        }
        return null;
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            fsQueue.clear();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (FileFormatException e) {
            // ignore
        } finally {
            lock.unlock();
        }
    }

    /**
     * 关闭文件队列
     *
     * @throws IOException
     * @throws FileFormatException
     */
    public void close() throws IOException, FileFormatException {
        if (fsQueue != null) {
            fsQueue.close();
        }
    }
}
