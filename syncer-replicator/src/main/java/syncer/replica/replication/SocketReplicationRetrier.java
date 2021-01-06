package syncer.replica.replication;

import syncer.replica.cmd.*;
import syncer.replica.cmd.impl.SelectCommand;
import syncer.replica.cmd.synccomand.SocketSyncCommand;
import syncer.replica.constant.CMD;
import syncer.replica.constant.SyncStatusType;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.SyncMode;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.PostCommandSyncEvent;
import syncer.replica.event.PreCommandSyncEvent;
import syncer.replica.event.SyncerEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.replication.retry.AbstractReplicationRetrier;
import syncer.replica.util.objectutil.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static syncer.replica.entity.Status.CONNECTED;
import static syncer.replica.entity.SyncMode.*;
import static syncer.replica.util.objectutil.Strings.format;
import static syncer.replica.util.objectutil.Strings.isEquals;
import static syncer.replica.util.objectutil.Tuples.of;


/**
 * @author zhanenqiang
 * @Description Redis 主从协议
 * @Date 2020/8/7
 */
@Slf4j
@Setter
@Getter
@AllArgsConstructor
@Builder
public class SocketReplicationRetrier extends AbstractReplicationRetrier {
    /**
     * redis主从命令
     */
    private SocketSyncCommand socketSyncCommand;
    private SocketReplication socketReplication;
    private Configuration configuration;
    protected ReplyParser replyParser;

    @Override
    public void retry(Replication replication) throws IOException {
        try {
            super.retry(replication);
        } catch (IncrementException e) {
            log.error("[TASKID {}] ERROR {}",configuration.getTaskId(), e.getMessage());
            socketSyncCommand.getReplication().doTaskStatusListener(socketReplication, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg(e.getMessage())
                    .build());
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isManualClosed() {
        return socketReplication.isClosed();
    }
    @Override
    protected boolean open() throws IOException, IncrementException {
        String replId = configuration.getReplId();
        long replOffset = configuration.getReplOffset();
        String reoffset=String.valueOf(replOffset >= 0 ? replOffset + 1 : replOffset);
        log.info("[TASKID {}] PSYNC {} {}",configuration.getTaskId(), replId, reoffset);
        socketSyncCommand.send(CMD.PSYNC.getBytes(), replId.getBytes(), reoffset.getBytes());
        final String reply = Strings.toString(socketSyncCommand.reply());
        if(replOffset<0){
            replicaType= SyncStatusType.RdbSync;
        }
        SyncMode mode = socketReplication.trySync(reply);
        if (mode == PSYNC && socketReplication.getStatus() == CONNECTED) {
            socketSyncCommand.heartbeat();
        } else if (mode == SYNC_LATER && socketReplication.getStatus() == CONNECTED) {
            return false;
        }
        if (socketReplication.getStatus() != CONNECTED) {
            return true;
        }

        socketReplication.submitEvent(new PreCommandSyncEvent());
        socketReplication.submitSyncerTaskEvent(SyncerTaskEvent
                .builder()
                .taskStatusType(TaskStatusType.COMMANDRUNING)
                .offset(configuration.getReplOffset())
                .replid(configuration.getReplId())
                .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                .msg("COMMANDRUNING")
                .build());
        replicaType= SyncStatusType.CommandSync;
        if (socketReplication.db != -1) {
            socketReplication.submitEvent(new SelectCommand(socketReplication.db));
        }
        final long[] offset = new long[1];
        while (socketReplication.getStatus() == CONNECTED) {
//            System.out.println("------"+len);
            Object obj = replyParser.parse(len -> offset[0] = len);
            if (obj instanceof Object[]) {
                if (socketReplication.verbose() && log.isDebugEnabled()){
                    log.debug("[TASKID {}] {}",configuration.getTaskId(),format((Object[]) obj));
                }
                Object[] raw = (Object[]) obj;
                CommandName name = CommandName.name(Strings.toString(raw[0]));
                final CommandParser<? extends Command> parser;
                if ((parser = socketReplication.commands.get(name)) == null) {
                    log.warn("[TASKID {}] command [{}] not register. raw command:{}",configuration.getTaskId(), name, format(raw));
                    configuration.addOffset(offset[0]);
                    offset[0] = 0L;
                    continue;
                }
                final long st = configuration.getReplOffset();
                final long ed = st + offset[0];
                if (isEquals(Strings.toString(raw[0]), CMD.SELECT)) {
                    socketReplication.db = CommandParsers.toInt(raw[1]);
                    socketReplication.submitEvent(parser.parse(raw), of(st, ed));
                } else if (isEquals(Strings.toString(raw[0]),CMD.REPLCONF) && isEquals(Strings.toString(raw[1]), CMD.GETACK)) {
                    if (mode == PSYNC) {
                        socketReplication.executor.execute(new Runnable() {
                                                               @Override
                                                               public void run() {
                                                                   socketSyncCommand.sendQuietly(CMD.REPLCONF.getBytes(), CMD.ACK.getBytes(), String.valueOf(configuration.getReplOffset()).getBytes());
                                                               }
                                                           }
                        );
                    }
                } else {
                    // include ping command
                    socketReplication.submitEvent(parser.parse(raw), of(st, ed));
                }
            } else {
                log.warn("[TASKID {}] unexpected redis reply:{}", configuration.getTaskId(),obj);
            }
            socketSyncCommand.getConfiguration().addOffset(offset[0]);
            offset[0] = 0L;
        }
        if (socketReplication.getStatus() == CONNECTED) {
            // should not reach here. add this line for code idempotent.
            socketReplication.submitEvent(new PostCommandSyncEvent());
            socketReplication.submitSyncerTaskEvent(SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.STOP)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("STOP")
                    .build());
        }
        return true;
    }

    @Override
    protected boolean connect() throws IOException ,IncrementException{
        socketReplication.establishConnection();
        return true;
    }

    @Override
    protected boolean close(IOException reason) throws IOException {
        if (reason != null){
            log.error("[TASKID {}] socket error. redis-server[{}:{}]", configuration.getTaskId(),socketReplication.host, socketReplication.port, reason);
        }

        socketReplication.doClose();
        if (reason != null) {
            log.info("[TASKID {}] reconnecting to redis-server[{}:{}]. retry times:{}", configuration.getTaskId(),socketReplication.host, socketReplication.port, (retries + 1));
        }
        return true;
    }


}
