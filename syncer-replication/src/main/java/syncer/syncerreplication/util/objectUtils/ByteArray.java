package syncer.syncerreplication.util.objectUtils;

import java.util.Iterator;

/**
 * @author zhanenqiang
 * @Description ByteArray 基于迭代器的byte数组 线程不安全
 * @Date 2020/4/7
 */
//@NonThreadSafe
public class ByteArray implements Iterable<byte[]> {

     static final int BITS = 30;
     static final int MAGIC = 1 << BITS;
     static final int MASK = MAGIC - 1;

    public static final long MIN_VALUE = 0L;
    public static final long MAX_VALUE = 2305843007066210304L; //(Integer.MAX_VALUE - 1) * MAGIC

     final int cap;
     final long length;
     byte[] smallBytes;
     byte[][] largeBytes;

    public ByteArray(byte[] smallBytes) {
        this(smallBytes, Integer.MAX_VALUE);
    }

    public ByteArray(long length) {
        this(length, Integer.MAX_VALUE);
    }

    public ByteArray(byte[] smallBytes, int cap) {
        this.cap = cap;
        this.length = smallBytes.length;
        this.smallBytes = smallBytes;
    }

    public ByteArray(long length, int cap) {
        this.cap = cap;
        this.length = length;
        if (length > MAX_VALUE || length < 0) {
            throw new IllegalArgumentException(String.valueOf(length));
        } else if (length <= cap) {
            this.smallBytes = new byte[(int) length];
        } else {
            final int x = (int) (length >> BITS);
            final int y = (int) (length & MASK);
            largeBytes = new byte[x + 1][];
            for (int i = 0; i < x; i++) {
                largeBytes[i] = new byte[MAGIC];
            }
            largeBytes[x] = new byte[y];
        }
    }

    public void set(long idx, byte value) {
        if (smallBytes != null) {
            smallBytes[(int) idx] = value;
            return;
        }
        int x = (int) (idx >> BITS);
        int y = (int) (idx & MASK);
        largeBytes[x][y] = value;
    }

    public byte get(long idx) {
        if (smallBytes != null){
            return smallBytes[(int) idx];
        }
        int x = (int) (idx >> BITS);
        int y = (int) (idx & MASK);
        return largeBytes[x][y];
    }

    public long length() {
        return this.length;
    }

    public byte[] first() {
        Iterator<byte[]> it = this.iterator();
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new Iter();
    }

    public static void arraycopy(ByteArray src, long srcPos, ByteArray dest, long destPos, long length) {
        if (srcPos + length > src.length || destPos + length > dest.length) {
            throw new IndexOutOfBoundsException();
        }
        if (srcPos + length <= src.cap && destPos + length <= dest.cap) {
            System.arraycopy(src.smallBytes, (int) srcPos, dest.smallBytes, (int) destPos, (int) length);
            return;
        }
        while (length > 0) {
            int x1 = (int) (srcPos >> BITS);
            int y1 = (int) (srcPos & MASK);
            int x2 = (int) (destPos >> BITS);
            int y2 = (int) (destPos & MASK);
            int min = Math.min(MAGIC - y1, MAGIC - y2);
            if (length <= MAGIC) {
                min = Math.min(min, (int) length);
            }
            System.arraycopy(src.largeBytes[x1], y1, dest.largeBytes[x2], y2, min);
            srcPos += min;
            destPos += min;
            length -= min;
        }
        assert length == 0;
    }

    protected class Iter implements Iterator<byte[]> {
        protected int index = 0;

        @Override
        public boolean hasNext() {
            if (smallBytes != null) {
                return index < 1;
            }
            return index < largeBytes.length;
        }

        @Override
        public byte[] next() {
            if (smallBytes != null) {
                index++;
                return smallBytes;
            }
            return largeBytes[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
