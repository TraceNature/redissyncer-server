package syncer.transmission.exception;

public class MultiCommitStartException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = -1L;

    /**
     * Constructs an {@code FileFormatException} with {@code null} as its error
     * detail message.
     */
    public MultiCommitStartException() {
        super();
    }

    public MultiCommitStartException(String message) {
        super(message);
    }

    public MultiCommitStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiCommitStartException(Throwable cause) {
        super(cause);
    }
}
