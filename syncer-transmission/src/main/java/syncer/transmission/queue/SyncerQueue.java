// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.queue;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public interface SyncerQueue<E>  extends Queue<E> {
    @Override
    boolean add(E e);

    @Override
    boolean offer(E e);


    void put(E e) throws InterruptedException;

    boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException;


    E take() throws InterruptedException;

    E poll(long timeout, TimeUnit unit)
            throws InterruptedException;


    int remainingCapacity();
    @Override
    boolean remove(Object o);
    @Override
    public boolean contains(Object o);

    int drainTo(Collection<? super E> c);

    int drainTo(Collection<? super E> c, int maxElements);
}
