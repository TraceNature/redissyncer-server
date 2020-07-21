package syncer.syncerservice.util.JDRedisClient.command;

import com.jd.jim.cli.protocol.CommandType;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/29
 */
public abstract class SyncerJimAbstrasctCommand {
    private byte[][]data;

    public SyncerJimAbstrasctCommand(byte[][] data) {
        this.data = data;
    }

    public byte[][] getData() {
        return data;
    }

    public void setData(byte[][] data) {
        this.data = data;
    }

    public abstract CommandType getCommand();
}
