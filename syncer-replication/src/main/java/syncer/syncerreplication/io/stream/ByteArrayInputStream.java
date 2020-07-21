package syncer.syncerreplication.io.stream;

import syncer.syncerreplication.util.objectUtils.ByteArray;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
public class ByteArrayInputStream  extends InputStream {

    protected long pos;
    protected long count;
    protected long mark = 0;
    protected ByteArray buf;

    public ByteArrayInputStream(ByteArray buf) {
        this.pos = 0;
        this.buf = buf;
        this.count = buf.length();
    }

    @Override
    public int read() {
        return (pos < count) ? (buf.get(pos++) & 0xff) : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count){
            return -1;
        }
        int avail = (int) (count - pos);
        if (len > avail) {
            len = avail;
        }
        if (len <= 0){
            return 0;
        }
        ByteArray.arraycopy(buf, pos, new ByteArray(b), off, len);
        pos += len;
        return len;
    }

    @Override
    public long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }
        pos += k;
        return k;
    }

    @Override
    public int available() {
        return (int) (this.count - this.pos);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) {
        mark = pos;
    }

    @Override
    public void reset() {
        pos = mark;
    }

    @Override
    public void close() throws IOException {
    }
}
