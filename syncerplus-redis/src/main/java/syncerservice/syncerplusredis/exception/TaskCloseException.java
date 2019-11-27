package syncerservice.syncerplusredis.exception;

public class TaskCloseException extends Exception {
    public TaskCloseException() {
        super();
    }

    public TaskCloseException(String message) {
        super(message);
    }
}
