package syncer.transmission.exception;

public class MultiCommitEndException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = -1L;

    /**
     * Constructs an {@code FileFormatException} with {@code null} as its error
     * detail message.
     */
    public MultiCommitEndException() {
        super();
    }

    public MultiCommitEndException(String message) {
        super(message);
    }

    public MultiCommitEndException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiCommitEndException(Throwable cause) {
        super(cause);
    }
}
