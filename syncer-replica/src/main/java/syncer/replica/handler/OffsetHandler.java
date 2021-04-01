package syncer.replica.handler;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
@FunctionalInterface
public interface OffsetHandler {
    void handle(long len);
}