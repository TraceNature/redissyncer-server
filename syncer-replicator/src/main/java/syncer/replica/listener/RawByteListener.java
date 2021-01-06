package syncer.replica.listener;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
@FunctionalInterface
public interface RawByteListener {
    void handle(byte... rawBytes);
}
