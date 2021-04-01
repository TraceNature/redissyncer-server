package syncer.replica.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author: Eq Zhan
 * @create: 2021-03-15
 **/
public class RedisOutputStream extends OutputStream {
    private  OutputStream out;
    public RedisOutputStream(OutputStream out) {
        this.out = new BufferedOutputStream(out);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void writeCrLf() throws IOException {
        out.write('\r');
        out.write('\n');
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
