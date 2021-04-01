package syncer.replica.parser.command.string;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.string.AppendCommand;

/**
 * APPEND key value
 * 自2.0.0起可用。
 *
 * 时间复杂度： O（1）。分摊的时间复杂度为O（1），假设附加值很小，并且已有值为任意大小，因为Redis使用的动态字符串库将使每次重新分配的可用空间加倍。
 *
 * 如果key已经存在，并且是一个字符串，则该命令将value在字符串的末尾附加。如果key不存在，它将被创建并设置为空字符串，因此 APPEND 在这种特殊情况下将与SET类似。
 *
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
 */
public class AppendCommandParser implements CommandParser<AppendCommand> {

    @Override
    public AppendCommand parse(Object[] command) {
        return new AppendCommand(CommandParsers.toBytes(command[1]), CommandParsers.toBytes(command[2]));
    }

}