package syncer.replica.rdb;

import syncer.replica.io.RedisInputStream;

import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public abstract class RedisRdbValueVisitor {

    public <T> T applyString(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyList(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applySet(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyZSet(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyZSet2(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyHash(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyHashZipMap(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyListZipList(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applySetIntSet(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyZSetZipList(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyHashZipList(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyListQuickList(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyModule(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyModule2(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }

    public <T> T applyStreamListPacks(RedisInputStream in, int version) throws IOException {
        throw new UnsupportedOperationException("must implement this method.");
    }
}
