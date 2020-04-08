package syncer.syncerreplication.util.objectUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author zhanenqiang
 * @Description 数组对象迭代器
 * @Date 2020/4/7
 */
@SuppressWarnings("unchecked")
public class Iterators {

    public static Iterator<?> EMPTY = new Iterator<Object>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }
    };

    @SafeVarargs
    public static <T> Iterator<T> iterator(final T... t) {
        class Iter implements Iterator<T> {
            private int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < t.length;
            }

            @Override
            public T next() {
                if (!hasNext()){
                    throw new NoSuchElementException();
                }
                return t[idx++];
            }
        }
        return t == null ? (Iterator<T>) EMPTY : new Iter();
    }
}
