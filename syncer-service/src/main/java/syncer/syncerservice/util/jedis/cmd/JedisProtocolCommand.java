package syncer.syncerservice.util.jedis.cmd;

import lombok.Builder;
import redis.clients.jedis.commands.ProtocolCommand;

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
