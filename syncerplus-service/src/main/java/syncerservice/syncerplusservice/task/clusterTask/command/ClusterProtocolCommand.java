package syncerservice.syncerplusservice.task.clusterTask.command;


import redis.clients.jedis.commands.ProtocolCommand;

/**
 * 命令模块
 */
public class ClusterProtocolCommand implements ProtocolCommand {
    private byte[] raw;

    public ClusterProtocolCommand(byte[] raw) {
        this.raw = raw;
    }


    @Override
    public byte[] getRaw() {
        return this.raw;
    }
}
