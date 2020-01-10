package syncer.syncerservice.exception;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/10
 */
public class FilterNodeException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = -1L;

    /**
     * Constructs an {@code FileFormatException} with {@code null} as its error
     * detail message.
     */
    public FilterNodeException() {
        super();
    }

    public FilterNodeException(String message) {
        super(message);
    }

    public FilterNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FilterNodeException(Throwable cause) {
        super(cause);
    }
}
