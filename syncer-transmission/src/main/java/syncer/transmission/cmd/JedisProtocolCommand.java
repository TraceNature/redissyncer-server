package syncer.transmission.cmd;

import lombok.Builder;
import syncer.jedis.commands.ProtocolCommand;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/24
 */
@Builder
public class JedisProtocolCommand implements ProtocolCommand {
    private byte[] raw;

    public JedisProtocolCommand(byte[] raw) {
        this.raw = raw;
    }

    @Override
    public byte[] getRaw() {
        return this.raw;
    }
}
