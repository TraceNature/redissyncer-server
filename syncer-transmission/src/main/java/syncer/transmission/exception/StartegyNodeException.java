package syncer.transmission.exception;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
public class StartegyNodeException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = -1L;

    /**
     * Constructs an {@code FileFormatException} with {@code null} as its error
     * detail message.
     */
    public StartegyNodeException() {
        super();
    }

    public StartegyNodeException(String message) {
        super(message);
    }

    public StartegyNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public StartegyNodeException(Throwable cause) {
        super(cause);
    }
}
