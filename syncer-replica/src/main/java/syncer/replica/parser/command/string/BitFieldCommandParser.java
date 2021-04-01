package syncer.replica.parser.command.string;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.string.*;
import syncer.replica.util.strings.Strings;

import java.util.ArrayList;
import java.util.List;


/**
 * BITFIELD key [GET type offset] [SET type offset value] [INCRBY type offset increment] [OVERFLOW WRAP|SAT|FAIL]
 * 自3.2.0起可用。
 *
 * 时间复杂度： O（1）用于指定的每个子命令
 *
 * 该命令将 Redis 字符串视为一个位数组，并且能够处理具有不同位宽和任意非（必要）对齐偏移量的特定整数字段。实际上，使用此命令可以将位偏移量为1234的带符号5位整数设置为特定值，从偏移量4567中检索31位无符号整数。类似地，该命令处理指定整数的递增和递减，提供保证和良好指定的溢出和下溢行为，用户可以配置。
 *
 * BITFIELD 能够在同一个命令调用中使用多位字段。它需要执行一系列操作，并返回一个响应数组，其中每个数组都与参数列表中的相应操作相匹配。
 *
 * 例如，以下命令将位偏移量为100的8位有符号整数加1，并在位偏移量0处获取4位无符号整数的值：
 *
 * > BITFIELD mykey INCRBY i5 100 1 GET u4 0
 * 1) (integer) 1
 * 2) (integer) 0
 * 注意：
 *
 * 使用当前字符串长度以外的 GET 位（包括密钥根本不存在的情况）进行寻址，结果执行的操作与缺失部分一样都是由设置为0的位组成。
 * 2. 根据需要的最小长度，根据所触及的最远位，使用当前字符串长度之外的 SET 或 INCRBY 位进行寻址将放大字符串，根据需要对其进行填零。
 *
 * 支持的子命令和整数类型
 * 以下是支持的命令列表。
 *
 * GET <type> <offset> - 返回指定的位域。
 * SET <type> <offset> <value> - 设置指定的位域并返回其旧值。
 * INCRBY <type> <offset> <increment> - 递增或递减（如果给定负递增）指定的位域并返回新值。
 * 还有一个子命令通过设置溢出行为来改变连续的 INCRBY子 命令调用的行为：
 *
 * OVERFLOW [WRAP|SAT|FAIL]在期望整数类型的情况下，可以通过i为有符号整数和u无符号整数加上整数类型的位数来构成它。例如u8，一个8位的无符号整数，i16是一个16位的有符号整数。支持的类型对于有符号整数最多为64位，对于无符号整数最多为63位。使用无符号整数的限制是由于当前Redis协议无法将64位无符号整数作为答复返回。位和位置偏移有两种方式可以指定位域命令中的偏移量。如果指定了一个没有任何前缀的数字，它将被用作字符串内的基于零的位偏移量。但是如果偏移量前缀为a#字符，指定的偏移量乘以整数类型的宽度，例如：BITFIELD mystring SET i8＃0 100 i8＃1 200将设置第一个i8整数在偏移量0和第二个偏移量为8.这种方式你没有如果你想要的是一个给定大小的整数数组，你可以在你的客户端内部进行数学运算。溢出控制使用该OVERFLOW命令，用户可以通过指定一个来微调增量的行为或减少溢出（或下溢）以下行为：
 * WRAP：环绕，包含有符号和无符号整数。在无符号整数的情况下，包装类似于以整数可以包含的最大值（C标准行为）来执行操作。使用带符号整数，而不是包装意味着溢出重新开始朝向最负值，并且溢出朝向最正值，例如，如果i8整数设置为127，则将其递增1 -128。
 * SAT：使用饱和算术，即在下溢时将该值设置为最小整数值，并在溢出时将其设置为最大整数值。例如，i8从数值120开始递增一个以10 为增量的整数将导致数值127，并且进一步增量将始终使数值保持在127.在下溢时发生同样的情况，但是朝向该数值被阻塞在最大负值。
 * FAIL：在这种模式下，没有检测到溢出或下溢操作。相应的返回值设置为 NULL，以向调用者发送信号。
 * 请注意，每条OVERFLOW语句只影响子命令列表中后面的 INCRBY命令，直到下一条OVERFLOW语句为止。
 *
 * 默认情况下，如果未另外指定，则使用 WRAP。
 *
 */
public class BitFieldCommandParser implements CommandParser<BitFieldCommand> {

    @Override
    public BitFieldCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        List<Statement> list = new ArrayList<>();
        if (idx < command.length) {
            String token;
            do {
                idx = parseStatement(idx, command, list);
                if (idx >= command.length) {
                    break;
                }
                token = CommandParsers.toRune(command[idx]);
            }
            while (token != null && (Strings.isEquals(token, "GET") || Strings.isEquals(token, "SET") || Strings.isEquals(token, "INCRBY")));
        }
        List<OverFlow> overflows = null;
        if (idx < command.length) {
            overflows = new ArrayList<>();
            do {
                OverFlow overFlow = new OverFlow();
                idx = parseOverFlow(idx, command, overFlow);
                overflows.add(overFlow);
                if (idx >= command.length) {
                    break;
                }
            } while (Strings.isEquals(CommandParsers.toRune(command[idx]), "OVERFLOW"));
        }

        return new BitFieldCommand(key, list, overflows);
    }

    private int parseOverFlow(int i, Object[] params, OverFlow overFlow) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "OVERFLOW");
        OverFlowType overflow;
        String keyword = CommandParsers.toRune(params[idx++]);
        if (Strings.isEquals(keyword, "WRAP")) {
            overflow = OverFlowType.WRAP;
        } else if (Strings.isEquals(keyword, "SAT")) {
            overflow = OverFlowType.SAT;
        } else if (Strings.isEquals(keyword, "FAIL")) {
            overflow = OverFlowType.FAIL;
        } else {
            throw new AssertionError("parse [BITFIELD] command error." + keyword);
        }
        List<Statement> list = new ArrayList<>();
        if (idx < params.length) {
            String token;
            do {
                idx = parseStatement(idx, params, list);
                if (idx >= params.length) {
                    break;
                }
                token = CommandParsers.toRune(params[idx]);
            }
            while (token != null && (Strings.isEquals(token, "GET") || Strings.isEquals(token, "SET") || Strings.isEquals(token, "INCRBY")));
        }
        overFlow.setOverFlowType(overflow);
        overFlow.setStatements(list);
        return idx;
    }

    private int parseStatement(int i, Object[] params, List<Statement> list) {
        int idx = i;
        String keyword = CommandParsers.toRune(params[idx++]);
        Statement statement;
        if (Strings.isEquals(keyword, "GET")) {
            GetTypeOffset getTypeOffset = new GetTypeOffset();
            idx = parseGet(idx - 1, params, getTypeOffset);
            statement = getTypeOffset;
        } else if (Strings.isEquals(keyword, "SET")) {
            SetTypeOffsetValue setTypeOffsetValue = new SetTypeOffsetValue();
            idx = parseSet(idx - 1, params, setTypeOffsetValue);
            statement = setTypeOffsetValue;
        } else if (Strings.isEquals(keyword, "INCRBY")) {
            IncrByTypeOffsetIncrement incrByTypeOffsetIncrement = new IncrByTypeOffsetIncrement();
            idx = parseIncrBy(idx - 1, params, incrByTypeOffsetIncrement);
            statement = incrByTypeOffsetIncrement;
        } else {
            return i;
        }
        list.add(statement);
        return idx;
    }

    private int parseIncrBy(int i, Object[] params, IncrByTypeOffsetIncrement incrByTypeOffsetIncrement) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "INCRBY");
        byte[] type = CommandParsers.toBytes(params[idx]);
        idx++;
        byte[] offset = CommandParsers.toBytes(params[idx]);
        idx++;
        long increment = CommandParsers.toLong(params[idx++]);
        incrByTypeOffsetIncrement.setType(type);
        incrByTypeOffsetIncrement.setOffset(offset);
        incrByTypeOffsetIncrement.setIncrement(increment);
        return idx;
    }

    private int parseSet(int i, Object[] params, SetTypeOffsetValue setTypeOffsetValue) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "SET");
        byte[] type = CommandParsers.toBytes(params[idx]);
        idx++;
        byte[] offset = CommandParsers.toBytes(params[idx]);
        idx++;
        long value = CommandParsers.toLong(params[idx++]);
        setTypeOffsetValue.setType(type);
        setTypeOffsetValue.setOffset(offset);
        setTypeOffsetValue.setValue(value);
        return idx;
    }

    private int parseGet(int i, Object[] params, GetTypeOffset getTypeOffset) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "GET");
        byte[] type = CommandParsers.toBytes(params[idx]);
        idx++;
        byte[] offset = CommandParsers.toBytes(params[idx]);
        idx++;
        getTypeOffset.setType(type);
        getTypeOffset.setOffset(offset);
        return idx;
    }

    private void accept(String actual, String expect) {
        if (Strings.isEquals(actual, expect)){
            return;
        }
        throw new AssertionError("expect " + expect + " but actual " + actual);
    }

}
