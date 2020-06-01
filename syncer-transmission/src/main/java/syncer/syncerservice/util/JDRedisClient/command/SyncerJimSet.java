package syncer.syncerservice.util.JDRedisClient.command;

import com.jd.jim.cli.protocol.CommandType;
import lombok.Getter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/29
 */
public class SyncerJimSet extends SyncerJimAbstrasctCommand{
    @Getter
    private byte[]key;
    @Getter
    private byte[]value;


    public SyncerJimSet(byte[][] data) {
        super(data);
        loading();
    }

    @Override
    public CommandType getCommand() {
        return CommandType.SET;
    }



    void loading(){
        key=this.getData()[0];
        value=this.getData()[1];
    }


}
