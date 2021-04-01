package syncer.replica.datatype.command.common;

import syncer.replica.datatype.command.GenericKeyCommand;

/**
 * EXPIRE key seconds
 * Available since 1.0.0.
 *
 * Time complexity: O(1)
 *
 * Set a timeout on key. After the timeout has expired, the key will automatically be deleted. A key with an associated timeout is often said to be volatile in Redis terminology.
 *
 * The timeout will only be cleared by commands that delete or overwrite the contents of the key, including DEL, SET, GETSET and all the *STORE commands. This means that all the operations that conceptually alter the value stored at the key without replacing it with a new one will leave the timeout untouched. For instance, incrementing the value of a key with INCR, pushing a new value into a list with LPUSH, or altering the field value of a hash with HSET are all operations that will leave the timeout untouched.
 *
 * The timeout can also be cleared, turning the key back into a persistent key, using the PERSIST command.
 *
 * If a key is renamed with RENAME, the associated time to live is transferred to the new key name.
 *
 * If a key is overwritten by RENAME, like in the case of an existing key Key_A that is overwritten by a call like RENAME Key_B Key_A, it does not matter if the original Key_A had a timeout associated or not, the new key Key_A will inherit all the characteristics of Key_B.
 *
 * Note that calling EXPIRE/PEXPIRE with a non-positive timeout or EXPIREAT/PEXPIREAT with a time in the past will result in the key being deleted rather than expired (accordingly, the emitted key event will be del, not expired).
 *
 * Refreshing expires
 * It is possible to call EXPIRE using as argument a key that already has an existing expire set. In this case the time to live of a key is updated to the new value. There are many useful applications for this, an example is documented in the Navigation session pattern section below.
 *
 * Differences in Redis prior 2.1.3
 * In Redis versions prior 2.1.3 altering a key with an expire set using a command altering its value had the effect of removing the key entirely. This semantics was needed because of limitations in the replication layer that are now fixed.
 *
 * EXPIRE would return 0 and not alter the timeout for a key with a timeout set.
 *
 * Return value
 * Integer reply, specifically:
 *
 * 1 if the timeout was set.
 * 0 if key does not exist.
 * Examples
 * redis> SET mykey "Hello"
 * "OK"
 * redis> EXPIRE mykey 10
 * (integer) 1
 * redis> TTL mykey
 * (integer) 10
 * redis> SET mykey "Hello World"
 * "OK"
 * redis> TTL mykey
 * (integer) -1
 * redis>
 */
public class ExpireCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    private int ex;

    public ExpireCommand() {
    }

    public ExpireCommand(byte[] key, int ex) {
        super(key);
        this.ex = ex;
    }

    public int getEx() {
        return ex;
    }

    public void setEx(int ex) {
        this.ex = ex;
    }
}
