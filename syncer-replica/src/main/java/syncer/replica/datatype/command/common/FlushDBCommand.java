package syncer.replica.datatype.command.common;

import syncer.replica.datatype.command.AbstractBaseCommand;

public class FlushDBCommand extends AbstractBaseCommand {

    private static final long serialVersionUID = 1L;

    private boolean async;
    private boolean sync;

    public FlushDBCommand() {
    }

    public FlushDBCommand(boolean async) {
        this(async, false);
    }

    /**
     * @since 3.5.2
     * @param async async
     * @param sync sync
     */
    public FlushDBCommand(boolean async, boolean sync) {
        this.async = async;
        this.sync = sync;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * @since 3.5.2
     * @return sync
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * @since 3.5.2
     * @param sync sync
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
