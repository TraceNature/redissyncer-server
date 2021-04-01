package syncer.replica.datatype.command.lua;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 * https://redis.io/commands/eval
 *
 * EVAL script numkeys key [key ...] arg [arg ...]
 * Available since 2.6.0.
 *
 * Time complexity: Depends on the script that is executed.
 *
 * Introduction to EVAL
 * EVAL and EVALSHA are used to evaluate scripts using the Lua interpreter built into Redis starting from version 2.6.0.
 *
 * The first argument of EVAL is a Lua 5.1 script. The script does not need to define a Lua function (and should not). It is just a Lua program that will run in the context of the Redis server.
 *
 * The second argument of EVAL is the number of arguments that follows the script (starting from the third argument) that represent Redis key names. The arguments can be accessed by Lua using the KEYS global variable in the form of a one-based array (so KEYS[1], KEYS[2], ...).
 *
 * All the additional arguments should not represent key names and can be accessed by Lua using the ARGV global variable, very similarly to what happens with keys (so ARGV[1], ARGV[2], ...).
 *
 * The following example should clarify what stated above:
 *
 * > eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
 * 1) "key1"
 * 2) "key2"
 * 3) "first"
 * 4) "second"
 */
public class EvalCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;

    private byte[] script;
    private int numkeys;
    private byte[][] keys;
    private byte[][] args;

    public EvalCommand() {
    }

    public EvalCommand(byte[] script, int numkeys, byte[][] keys, byte[][] args) {
        this.script = script;
        this.numkeys = numkeys;
        this.keys = keys;
        this.args = args;
    }

    public byte[] getScript() {
        return script;
    }

    public void setScript(byte[] script) {
        this.script = script;
    }

    public int getNumkeys() {
        return numkeys;
    }

    public void setNumkeys(int numkeys) {
        this.numkeys = numkeys;
    }

    public byte[][] getKeys() {
        return keys;
    }

    public void setKeys(byte[][] keys) {
        this.keys = keys;
    }

    public byte[][] getArgs() {
        return args;
    }

    public void setArgs(byte[][] args) {
        this.args = args;
    }
}
