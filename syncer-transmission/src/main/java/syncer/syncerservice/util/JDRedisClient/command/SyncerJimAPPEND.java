package syncer.syncerservice.util.JDRedisClient.command;

import com.jd.jim.cli.protocol.CommandType;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/29
 */
public class SyncerJimAPPEND   extends SyncerJimSet{

    public SyncerJimAPPEND(byte[][] data) {
        super(data);
    }



    @Override
    public CommandType getCommand() {
        return CommandType.APPEND;
    }
}
