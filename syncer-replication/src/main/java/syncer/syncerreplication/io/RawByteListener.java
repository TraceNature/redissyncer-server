package syncer.syncerreplication.io;

/**
 * @author zhanenqiang
 * @Description 二进制格式监听器
 * @Date 2020/4/7
 */
@FunctionalInterface
public interface RawByteListener {
    void handle(byte... rawBytes);
}
