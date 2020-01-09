package syncer.syncerservice.util.queue;

import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/30
 */
public class DbMapLocalDiskMemoryQueue <E>  implements SyncerQueue<E> {
    private String taskId;
    private BlockingQueue<Object> queue;
    private int size=1000;
    public static void main(String[] args) {
        DBMaker.newFileDB(new File("test.db")).make().createCircularQueue("queue", Serializer.JAVA,1000);

    }

    public DbMapLocalDiskMemoryQueue(String taskId, int size) {
        this.taskId = taskId;
        this.queue=DBMaker.newFileDB(new File("syncer_"+taskId+".db")).mmapFileEnableIfSupported().make().createCircularQueue(taskId, Serializer.JAVA,size);
        if(size>0){
            this.size = size;
        }
    }

    @Override
    public boolean add(E e) {
        return  queue.add(e);
    }

    @Override
    public boolean offer(E e) {
        return  queue.offer(e);
    }

    @Override
    public E remove() {
        return (E) queue.remove();
    }

    @Override
    public E poll() {
        return (E) queue.poll();
    }

    @Override
    public E element() {
        return (E) queue.element();
    }

    @Override
    public E peek() {
        return (E) queue.peek();
    }

    @Override
    public void put(E e) throws InterruptedException {
       queue.put(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return queue.offer(e,timeout,unit);
    }

    @Override
    public E take() throws InterruptedException {
        return (E) queue.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return (E) queue.poll(timeout,unit);
    }

    @Override
    public int remainingCapacity() {
        return  queue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return  queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return  queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return  queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return  queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return  queue.retainAll(c);
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
        return  queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return  queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return (Iterator<E>) queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return  queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return  queue.toArray(a);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return  queue.drainTo((Collection<? super Object>) c);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return  queue.drainTo((Collection<? super Object>) c,maxElements);
    }
}
