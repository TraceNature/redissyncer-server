package syncer.replica.parser.command.string;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.string.BitOpCommand;
import syncer.replica.datatype.command.string.Op;


/**
 *BITOP operation destkey key [key ...]
 * 自2.6.0起可用。
 *
 * 时间复杂度： O（N）
 *
 * 在多个键（包含字符串值）之间执行按位操作并将结果存储在目标键中。
 *
 * BITOP 命令支持四个按位运算：AND，OR，XOR和NOT，因此调用该命令的有效形式为：
 *
 * BITOP AND destkey srckey1 srckey2 srckey3 ... srckeyN
 * BITOP OR  destkey srckey1 srckey2 srckey3 ... srckeyN
 * BITOP XOR destkey srckey1 srckey2 srckey3 ... srckeyN
 * BITOP NOT destkey srckey
 * 正如你可以看到，NOT 是特殊的，因为它只需要一个输入键，因为它执行比特反转，所以它只作为一元运算符有意义。
 *
 * 操作结果始终存储在destkey。
 */
public class BitOpCommandParser  implements CommandParser<BitOpCommand> {
    @Override
    public BitOpCommand parse(Object[] command) {
        int idx = 1;
        String strOp = CommandParsers.toRune(command[idx++]);
        Op op = Op.valueOf(strOp.toUpperCase());
        byte[] destKey = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[][] keys = new byte[command.length - 3][];
        for (int i = idx, j = 0; i < command.length; i++, j++) {
            keys[j] = CommandParsers.toBytes(command[i]);
        }
        return new BitOpCommand(op, destKey, keys);
    }

}
