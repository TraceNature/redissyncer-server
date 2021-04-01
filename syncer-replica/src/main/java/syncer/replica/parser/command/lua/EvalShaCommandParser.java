package syncer.replica.parser.command.lua;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.lua.EvalShaCommand;

import java.util.ArrayList;
import java.util.List;

/**
 *EVALSHA sha1 numkeys key [key ...] arg [arg ...]
 * Available since 2.6.0.
 *
 * Time complexity: Depends on the script that is executed.
 *
 * Evaluates a script cached on the server side by its SHA1 digest. Scripts are cached on the server side using the SCRIPT LOAD command. The command is otherwise identical to EVAL.
 */

public class EvalShaCommandParser implements CommandParser<EvalShaCommand> {
    @Override
    public EvalShaCommand parse(Object[] command) {
        int idx = 1;
        byte[] sha = CommandParsers.toBytes(command[idx]);
        idx++;
        int numkeys = CommandParsers.toInt(command[idx++]);
        byte[][] keys = new byte[numkeys][];
        for (int i = 0; i < numkeys; i++) {
            keys[i] = CommandParsers.toBytes(command[idx]);
            idx++;
        }
        List<byte[]> list = new ArrayList<>();
        while (idx < command.length) {
            list.add(CommandParsers.toBytes(command[idx]));
            idx++;
        }
        byte[][] args = new byte[list.size()][];
        list.toArray(args);
        return new EvalShaCommand(sha, numkeys, keys, args);
    }

}
