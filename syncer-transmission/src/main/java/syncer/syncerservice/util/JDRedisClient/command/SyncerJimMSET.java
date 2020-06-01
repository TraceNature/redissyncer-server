package syncer.syncerservice.util.JDRedisClient.command;

import com.jd.jim.cli.protocol.CommandType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/29
 */
public class SyncerJimMSET extends SyncerJimAbstrasctCommand {
    @Getter
    private byte[]key;
    @Getter
    private Map<byte[], byte[]> tuple;

    public SyncerJimMSET(byte[][] data) {
        super(data);
        loading();
    }

    void loading(){
        key=this.getData()[0];
        Map<byte[], byte[]> tupleData=new HashMap<>();
        for (int i = 1; i < getData().length; i+=2) {
            tupleData.put(getData()[i],getData()[i+1]);
        }
        this.tuple=tupleData;
    }

    @Override
    public CommandType getCommand() {
        return CommandType.MSET;
    }
}
