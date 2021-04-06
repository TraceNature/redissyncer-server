package syncer.replica.parser.command.common;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.CopyCommand;
import syncer.replica.util.strings.Strings;


/**
 * https://redis.io/commands/copy
 *
 * COPY source destination [DB destination-db] [REPLACE]
 * Available since 6.2.0.
 *
 * Time complexity: O(N) worst case for collections, where N is the number of nested items. O(1) for string values.
 *
 * This command copies the value stored at the source key to the destination key.
 *
 * By default, the destination key is created in the logical database used by the connection.
 * The DB option allows specifying an alternative logical database index for the destination key.
 *
 * The command returns an error when the destination key already exists. The REPLACE option removes the destination key before copying the value to it.
 *
 * Return value
 * Integer reply, specifically:
 *
 * 1 if source was copied.
 * 0 if source was not copied.
 * Examples
 * SET dolly "sheep"
 * COPY dolly clone
 * GET clone
 */
public class CopyCommandParser implements CommandParser<CopyCommand> {

    @Override
    public CopyCommand parse(Object[] command) {
        int idx = 1;
        byte[] source = CommandParsers.toBytes(command[idx++]);
        byte[] destination = CommandParsers.toBytes(command[idx++]);
        boolean replace = false;
        Integer db = null;
        for (int i = idx; i < command.length; i++) {
            String str = CommandParsers.toRune(command[i]);
            if (Strings.isEquals(str, "REPLACE")) {
                replace = true;
            } else if (Strings.isEquals(str, "DB")) {
                i++;
                db = CommandParsers.toInt(command[i]);
            } else {
                throw new AssertionError("parse [COPY] command error." + str);
            }
        }
        return new CopyCommand(source, destination, db, replace);
    }
}

