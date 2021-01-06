package syncer.transmission.exception;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/7
 */
public class TaskErrorException extends Exception{
    private static final long serialVersionUID = -1L;

    public TaskErrorException() {
        super();
    }

    public TaskErrorException(String message) {
        super(message);
    }

    public TaskErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskErrorException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
