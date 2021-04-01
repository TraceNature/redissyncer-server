package syncer.replica.datatype.command.lua;

import syncer.replica.datatype.command.AbstractBaseCommand;

/**
 *EVALSHA sha1 numkeys key [key ...] arg [arg ...]
 * Available since 2.6.0.
 *
 * Time complexity: Depends on the script that is executed.
 *
 * Evaluates a script cached on the server side by its SHA1 digest. Scripts are cached on the server side using the SCRIPT LOAD command. The command is otherwise identical to EVAL.
 */
public class EvalShaCommand extends AbstractBaseCommand {
    private static final long serialVersionUID = 1L;
    private byte[] sha;
    private int numkeys;
    private byte[][] keys;
    private byte[][] args;

    public EvalShaCommand() {
    }

    public EvalShaCommand(byte[] sha, int numkeys, byte[][] keys, byte[][] args) {
        this.sha = sha;
        this.numkeys = numkeys;
        this.keys = keys;
        this.args = args;
    }

    public byte[] getSha() {
        return sha;
    }

    public void setSha(byte[] sha) {
        this.sha = sha;
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
