package syncer.replica.datatype.command.transactions;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 * https://redis.io/commands/exec
 * EXEC
 * Available since 1.2.0.
 *
 * Executes all previously queued commands in a transaction and restores the connection state to normal.
 *
 * When using WATCH, EXEC will execute commands only if the watched keys were not modified, allowing for a check-and-set mechanism.
 *
 * Return value
 * Array reply: each element being the reply to each of the commands in the atomic transaction.
 *
 * When using WATCH, EXEC can return a Null reply if the execution was aborted.
 */
public class ExecCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;

    public ExecCommand() {
    }
}
