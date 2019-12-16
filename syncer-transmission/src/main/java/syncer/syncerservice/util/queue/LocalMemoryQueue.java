package syncer.syncerservice.util.queue;

import lombok.Builder;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class LocalMemoryQueue<E> implements SyncerQueue<E> {
    private LinkedBlockingQueue<E> queue;
    private String taskId;

    public LocalMemoryQueue( String taskId,int capacity) {
        this.queue =new LinkedBlockingQueue<>(capacity);
        this.taskId = taskId;
    }

    @Override
    public boolean add(E e) {
        return queue.add(e);
    }

    @Override
    public boolean offer(E e) {
        return queue.offer(e);
    }

    @Override
    public E remove() {
        return queue.remove();
    }

    @Override
    public E poll() {
        return queue.poll();
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
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
        return queue.take();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout,unit);
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return queue.toArray(a);
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return queue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return queue.drainTo(c,maxElements);
    }
}
