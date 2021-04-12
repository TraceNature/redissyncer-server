package syncer.replica.retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.cmd.CMD;
import syncer.replica.config.ReplicConfig;
import syncer.replica.datatype.command.Command;
import syncer.replica.datatype.command.CommandName;
import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.common.SelectCommand;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.exception.RedisAuthErrorException;
import syncer.replica.parser.protocol.ProtocolReplyParser;
import syncer.replica.protocol.DefaultSyncRedisProtocol;
import syncer.replica.replication.SocketReplication;
import syncer.replica.replication.Replication;
import syncer.replica.status.TaskStatus;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.tuple.Tuples;
import syncer.replica.util.type.CapaSyncType;
import syncer.replica.util.type.SyncStatusType;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * @author: Eq Zhan
 * @create: 2021-03-19
 **/
@Slf4j
@Setter
@Getter
@AllArgsConstructor
@Builder
public class SocketReplicationRetrier extends AbstractReplicationRetrier{
    /**
     * redis主从命令
     */
    private DefaultSyncRedisProtocol syncRedisProtocol;
    private SocketReplication socketReplication;
    private ReplicConfig config;
    protected ProtocolReplyParser protocolReplyParser;

    @Override
    public boolean isManualClosed() {
        return socketReplication.isClosed();
    }

    public void setReplyParser(ProtocolReplyParser protocolReplyParser) {
        this.protocolReplyParser = protocolReplyParser;
    }

    @Override
    public boolean open() throws IOException, IncrementException, RedisAuthErrorException {
        String replId = config.getReplId();
        long replOffset = config.getReplOffset();
        String reoffset=String.valueOf(replOffset >= 0 ? replOffset + 1 : replOffset);
        log.info("[TASKID {}] PSYNC {} {}",config.getTaskId(), replId, reoffset);
        syncRedisProtocol.send(CMD.PSYNC.getBytes(), replId.getBytes(), reoffset.getBytes());
        final String reply = Strings.toString(syncRedisProtocol.reply());
        if(replOffset<0){
            replicaType= SyncStatusType.RdbSync;
        }

        CapaSyncType capaSyncType = socketReplication.trySync(reply);

        if(capaSyncType==CapaSyncType.PSYNC&&socketReplication.isRunning()){
            syncRedisProtocol.heartbeat();
        }else if(capaSyncType==CapaSyncType.SYNC_LATER && socketReplication.isRunning()){
            log.warn("[TASKID {}] reply : {}",config.getTaskId(),reply);
            return false;
        }
        if(!socketReplication.isRunning()){
            return true;
        }


        //增量标志
        socketReplication.submitEvent(new PreCommandSyncEvent());
        //通知状态订阅
        socketReplication.submitSyncerTaskEvent(SyncerTaskEvent
                .builder()
                .taskId(config.getTaskId())
                .event(TaskStatus.COMMANDRUNNING)
                .offset(config.getReplOffset())
                .replid(config.getReplId())
                .msg("COMMANDRUNING")
                .build());

        replicaType= SyncStatusType.CommandSync;

        if (socketReplication.getDb() != -1) {
            socketReplication.submitEvent(new SelectCommand(socketReplication.getDb()));
        }
        final long[] offset = new long[1];
        //
        while (socketReplication.isRunning()){
            Object obj = protocolReplyParser.parse(len -> offset[0] = len);
            if(obj instanceof Object[]){
                Object[] raws = (Object[]) obj;
                CommandName name = CommandName.name(Strings.toString(raws[0]));
                final CommandParser<? extends Command> parser;
                if(Objects.isNull(parser=socketReplication.getCommandParser(name))){
                    log.warn("[TASKID {}] command [{}] not register. raw command:{}",config.getTaskId(),name,Strings.format(raws));
                    config.addOffset(offset[0]);
                    offset[0] = 0L;
                    continue;
                }
                final long startOffset = config.getReplOffset();
                final long endOffset = startOffset + offset[0];
                //SELECT
                if(Strings.isEquals(CMD.SELECT,Strings.toString(raws[0]))){
                    socketReplication.setDb(CommandParsers.toInt(raws[1]));
                    socketReplication.submitEvent(parser.parse(raws), Tuples.of(startOffset, endOffset),config.getReplId(),endOffset);
                }else if(Strings.isEquals(CMD.REPLCONF,Strings.toString(raws[0]))&&Strings.isEquals(CMD.GETACK,Strings.toString(raws[1]))){
                    //在每次进入IO多路复用的等待事件前，Redis会调用beforeSleep函数，
                    //该函数会给所有slave发送REPLCONF GETACK命令，收到该命令的slave会马上发送自己的复制偏移量给master
                    //REPLCONF ACK {offset}
                    if(capaSyncType.equals(CapaSyncType.PSYNC)){
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                syncRedisProtocol.sendQuietly(CMD.REPLCONF.getBytes(),CMD.ACK.getBytes(),String.valueOf(config.getReplOffset()).getBytes());
                                log.info("[TASKID {}] get GETACK command and send [REPLCONF ACK {}] to master {}",config.getTaskId(),config.getReplOffset(),config.getReplId());
                            }
                        });
                    }

                }else {
                    // include ping command
                    socketReplication.submitEvent(parser.parse(raws), Tuples.of(startOffset, endOffset),config.getReplId(),endOffset);
                }

            } else {
                log.warn("[TASKID {}] unexpected redis reply:{}", config.getTaskId(),obj);
            }
            //add offset
            config.addOffset(offset[0]);
            offset[0] = 0L;
        }
        if(socketReplication.isRunning()){
            // 理论上只有aof文件结束时会到达这里
            // 此时应进入finish状态
            socketReplication.getConnected().set(TaskStatus.FINISH);
            socketReplication.submitEvent(new PostCommandSyncEvent());
            socketReplication.submitSyncerTaskEvent(SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.FINISH)
                    .offset(config.getReplOffset())
                    .replid(config.getReplId())
                    .taskId(config.getTaskId())
                    .msg("增量完成")
                    .build());
        }
        return true;
    }


    @Override
    public void retry(Replication replication) throws IOException, IncrementException {
        try {
            super.retry(replication);
        } catch (IncrementException e) {
            throw e;
        }
    }

    @Override
    public boolean connect() throws IOException, IncrementException, RedisAuthErrorException {
        socketReplication.establishConnection();
        return true;
    }


    @Override
    public boolean close(IOException reason) throws IOException {
        if (Objects.nonNull(reason)){
            log.error("[TASKID {}] socket error. redis-server[{}:{}]", config.getTaskId(),socketReplication.getHost(), socketReplication.getPort(), reason);
        }

        socketReplication.doClose();
        if (Objects.nonNull(reason)) {
            log.info("[TASKID {}] reconnecting to redis-server[{}:{}]. retry times:{}", config.getTaskId(),socketReplication.getHost(), socketReplication.getPort(), (retries + 1));
        }
        return true;
    }
}
