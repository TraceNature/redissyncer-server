package syncer.transmission.cmd;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/24
 */

import lombok.Builder;
import syncer.jedis.commands.ProtocolCommand;

/**
 * 命令模块
 */
@Builder
public class ClusterProtocolCommand implements ProtocolCommand {
    private byte[] raw;

    public ClusterProtocolCommand(byte[] raw) {
        this.raw = raw;
    }


    @Override
    public byte[] getRaw() {
        return this.raw;
    }

//    public static void main(String[] args) {
//        int a=1589777460000;
//    }
}