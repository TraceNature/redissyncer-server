package syncer.syncerplusredis.cmd.jimdb;

import syncer.syncerplusredis.cmd.CommandParser;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.util.objectutil.Strings;

/**
 * @author zhanenqiang
 * @Description jimdb增量命令首次解析---》 [transmit bj 1 set y y] --》[set y y]
 * @Date 2020/4/29
 */
public class JimDbFirstCommandParser implements CommandParser<JimdbFirstCommand> {

    @Override
    public JimdbFirstCommand parse(Object[] command) {
        Object[]data=new Object[command.length - 3];
        for (int i = 3, j = 0; i < command.length; i++) {
            data[j++]=command[i];
        }
        return new JimdbFirstCommand(data);
    }
}
