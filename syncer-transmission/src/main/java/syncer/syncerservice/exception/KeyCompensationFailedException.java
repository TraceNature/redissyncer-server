package syncer.syncerservice.exception;

/**
 * @author zhanenqiang
 * @Description key补偿失败
 * @Date 2020/7/23
 */
public class KeyCompensationFailedException  extends Exception  {
    public KeyCompensationFailedException() {
        super();
    }

    public KeyCompensationFailedException(String message) {
        super(message);
    }
}
