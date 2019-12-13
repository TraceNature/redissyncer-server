package syncer.syncerservice.util.queue;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public interface SyncerQueue<E>  extends Queue<E> {

    boolean add(E e);


    boolean offer(E e);


    void put(E e) throws InterruptedException;

    boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException;


    E take() throws InterruptedException;

    E poll(long timeout, TimeUnit unit)
            throws InterruptedException;


    int remainingCapacity();

    boolean remove(Object o);

    public boolean contains(Object o);

    int drainTo(Collection<? super E> c);

    int drainTo(Collection<? super E> c, int maxElements);
}
