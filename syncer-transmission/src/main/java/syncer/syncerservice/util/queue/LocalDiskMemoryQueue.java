package syncer.syncerservice.util.queue;

import syncer.syncerservice.exception.FileFormatException;
import syncer.syncerservice.util.jedis.ObjectUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalDiskMemoryQueue<E>  implements SyncerQueue<E>  {
    private String taskId;
    private FQueue queue;
    //用于记录容器中元素的个数
    private AtomicInteger count=new AtomicInteger(0);
    //申明锁
    private Lock lock = new ReentrantLock();
    //标识，可以表示具体的线程
    private final Condition conditionNull = lock.newCondition();
    private final Condition conditionFull = lock.newCondition();



    public LocalDiskMemoryQueue( String taskId,int fileNum) {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("data/");
        stringBuilder.append(taskId);
        stringBuilder.append("/"+fileNum);
        try {
            this.queue =new FQueue(stringBuilder.toString(),4 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileFormatException e) {
            e.printStackTrace();
        }
        this.taskId = taskId;
    }

    @Override
    public boolean add(E e) {
        return  queue.add(ObjectUtils.toBytes(e));
    }

    @Override
    public boolean offer(E e) {
        return  queue.offer(ObjectUtils.toBytes(e));
    }

    @Override
    public E remove() {
      return (E) ObjectUtils.toObject(queue.remove());
    }

    @Override
    public E poll() {
        return (E) ObjectUtils.toObject(queue.poll());
    }

    @Override
    public E element() {
        return (E) ObjectUtils.toObject(queue.element());
    }

    @Override
    public E peek() {
        return (E) ObjectUtils.toObject(queue.peek());
    }

    @Override
    public void put(E e) throws InterruptedException {

       queue.offer(ObjectUtils.toBytes(e));
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public E take() throws InterruptedException {
        return (E) ObjectUtils.toObject(queue.take());
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public boolean remove(Object o) {

        try {
            queue.remove();
        }catch (Exception e){

        }
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public int size() {
       return  queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>) queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
}
