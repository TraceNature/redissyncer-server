package syncer.replica.exception;

/**
 * tikv key error
 */
public class TikvKeyErrorException extends Exception{
    public TikvKeyErrorException(String message) {
        super(message);
    }
}
