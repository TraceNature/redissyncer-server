package syncer.replica.listener;

/**
 * @author: Eq Zhan
 * @create: 2021-03-12
 **/
@FunctionalInterface
public interface TaskRawByteListener {
    void handler(byte... rawBytes);
}
