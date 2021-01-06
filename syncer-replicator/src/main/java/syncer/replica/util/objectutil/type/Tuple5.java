/*
 * Copyright 2018-2019 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package syncer.replica.util.objectutil.type;



import syncer.replica.util.objectutil.Iterators;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * @author Leon Chen
 */
@SuppressWarnings("unchecked")
public class Tuple5<T1, T2, T3, T4, T5> implements Iterable<Object>, Serializable {
    private static final long serialVersionUID = 1L;
    private T1 v1;
    private T2 v2;
    private T3 v3;
    private T4 v4;
    private T5 v5;

    public Tuple5() {
    }

    public Tuple5(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
    }

    public Tuple5(Tuple5<T1, T2, T3, T4, T5> rhs) {
        this.v1 = rhs.getV1();
        this.v2 = rhs.getV2();
        this.v3 = rhs.getV3();
        this.v4 = rhs.getV4();
        this.v5 = rhs.getV5();
    }

    public T1 getV1() {
        return v1;
    }

    public T2 getV2() {
        return v2;
    }

    public T3 getV3() {
        return v3;
    }

    public T4 getV4() {
        return v4;
    }

    public T5 getV5() {
        return v5;
    }

    public void setV1(T1 v1) {
        this.v1 = v1;
    }

    public void setV2(T2 v2) {
        this.v2 = v2;
    }

    public void setV3(T3 v3) {
        this.v3 = v3;
    }

    public void setV4(T4 v4) {
        this.v4 = v4;
    }

    public void setV5(T5 v5) {
        this.v5 = v5;
    }

    public <V1, V2, V3, V4, V5> Tuple5<V1, V2, V3, V4, V5> map(Function<Tuple5<T1, T2, T3, T4, T5>, Tuple5<V1, V2, V3, V4, V5>> function) {
        return function.apply(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;

        if (v1 != null ? !v1.equals(tuple5.v1) : tuple5.v1 != null) {
            return false;
        }
        if (v2 != null ? !v2.equals(tuple5.v2) : tuple5.v2 != null) {
            return false;
        }
        if (v3 != null ? !v3.equals(tuple5.v3) : tuple5.v3 != null) {
            return false;
        }
        if (v4 != null ? !v4.equals(tuple5.v4) : tuple5.v4 != null) {
            return false;
        }
        return v5 != null ? v5.equals(tuple5.v5) : tuple5.v5 == null;
    }

    @Override
    public int hashCode() {
        int result = v1 != null ? v1.hashCode() : 0;
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        result = 31 * result + (v3 != null ? v3.hashCode() : 0);
        result = 31 * result + (v4 != null ? v4.hashCode() : 0);
        result = 31 * result + (v5 != null ? v5.hashCode() : 0);
        return result;
    }

    @Override
    public Iterator<Object> iterator() {
        return Iterators.iterator(getV1(), getV2(), getV3(), getV4(), getV5());
    }

    @Override
    public String toString() {
        return "[" + v1 + ", " + v2 + ", " + v3 + ", " + v4 + ", " + v5 + "]";
    }

    public static <V> Tuple5<V, V, V, V, V> from(V... ary) {
        if (ary == null || ary.length != 5) {
            throw new IllegalArgumentException();
        }
        return new Tuple5<>(ary[0], ary[1], ary[2], ary[3], ary[4]);
    }

    public static <V> Tuple5<V, V, V, V, V> from(Iterator<V> iterator) {
        List<V> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return from(list.toArray((V[]) new Object[list.size()]));
    }

    public static <V> Tuple5<V, V, V, V, V> from(Iterable<V> iterable) {
        return from(iterable.iterator());
    }

    public static <V> Tuple5<V, V, V, V, V> from(Collection<V> collection) {
        return from((Iterable<V>) collection);
    }

    public Object[] toArray() {
        return new Object[]{getV1(), getV2(), getV3(), getV4(), getV5()};
    }

    public <T> T[] toArray(Class<T> clazz) {
        T[] ary = (T[]) Array.newInstance(clazz, 5);
        if (!clazz.isInstance(getV1())){
            throw new UnsupportedOperationException();
        }
        ary[0] = (T) getV1();
        if (!clazz.isInstance(getV2())){
            throw new UnsupportedOperationException();
        }
        ary[1] = (T) getV2();
        if (!clazz.isInstance(getV3())) {
            throw new UnsupportedOperationException();
        }
        ary[2] = (T) getV3();
        if (!clazz.isInstance(getV4())) {
            throw new UnsupportedOperationException();
        }
        ary[3] = (T) getV4();
        if (!clazz.isInstance(getV5())){
            throw new UnsupportedOperationException();
        }
        ary[4] = (T) getV5();
        return ary;
    }

    public <T> T toObject(Function<Tuple5<T1, T2, T3, T4, T5>, T> func) {
        return func.apply(this);
    }
}
