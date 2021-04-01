package syncer.replica.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Leon Chen
 * @since 2.4.0
 */
public class PeekableInputStream extends InputStream {

    private int peek;
    private InputStream in;
    private boolean peeked;

    public PeekableInputStream(InputStream in) {
        this.in = in;
    }

    public int peek() throws IOException {
        if (!this.peeked) {
            this.peeked = true;
            return this.peek = this.in.read();
        }
        return this.peek;
    }

    @Override
    public int read() throws IOException {
        if (!this.peeked) {
            return in.read();
        }
        this.peeked = false;
        return this.peek;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        if (length <= 0) {
            return 0;
        }
        if (!this.peeked) {
            return in.read(b, offset, length);
        }
        this.peeked = false;
        if (this.peek < 0){
            return this.peek;
        }
        int len = in.read(b, offset + 1, length - 1);
        b[offset] = (byte) this.peek;
        return len < 0 ? 1 : len + 1;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        if (!this.peeked) {
            return this.in.skip(n);
        }
        this.peeked = false;
        return this.in.skip(n - 1) + 1;
    }

    @Override
    public int available() throws IOException {
        return (this.peeked ? 1 : 0) + this.in.available();
    }

    @Override
    public void close() throws IOException {
        this.in.close();
        this.peeked = false;
    }
}
