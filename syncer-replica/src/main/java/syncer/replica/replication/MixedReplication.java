package syncer.replica.replication;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.config.ReplicConfig;
import syncer.replica.datatype.command.Command;
import syncer.replica.datatype.command.CommandName;
import syncer.replica.datatype.command.CommandParser;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.PeekableInputStream;
import syncer.replica.io.RedisInputStream;
import syncer.replica.parser.RedisSyncerRdbParser;
import syncer.replica.parser.protocol.ProtocolReplyParser;
import syncer.replica.protocol.DefaultSyncRedisProtocol;
import syncer.replica.socket.NetStream;
import syncer.replica.status.TaskStatus;
import syncer.replica.util.code.RedisCodec;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.tuple.Tuples;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class MixedReplication extends AbstractReplication{
    protected final ProtocolReplyParser replyParser;
    protected final PeekableInputStream peekable;
    protected DefaultSyncRedisProtocol syncRedisProtocol;
    public MixedReplication(File file, ReplicConfig config) throws FileNotFoundException {
        this(new FileInputStream(file), config);
    }

    public MixedReplication(InputStream in, ReplicConfig config) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(config);
        this.config = config;
        if (in instanceof PeekableInputStream) {
            this.peekable = (PeekableInputStream) in;
        } else {
            in = this.peekable = new PeekableInputStream(in);
        }
        this.inputStream = new RedisInputStream(in, this.config.getBufferSize());
        this.inputStream.setRawByteListenerList(this.rawByteListenerList);
        this.replyParser = new ProtocolReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();

    }

    public MixedReplication(String filePath, ReplicConfig config){
        this(filePath,config,false);
    }

    public MixedReplication(String filePath, ReplicConfig config ,boolean online) {
        InputStream in = null;
        try {
            if(online){
                NetStream netStream= NetStream.builder().build();
                in=netStream.getInputStreamByOnlineFile(filePath,config);
            }else {
                in = new FileInputStream(filePath);
                try {
                    config.setFileSize(in.available());
                }catch (Exception e){
                    log.error("获取在本地数据文件大小失败...");
                }
            }
        } catch (FileNotFoundException e) {
            connected.set(TaskStatus.BROKEN);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .taskId(config.getTaskId())
                    .replid(config.getReplId())
                    .msg(e.getMessage())
                    .build());
            log.warn("任务Id【{}】异常停止，停止原因【{}】", config.getTaskId(), "本地文件未找到");
        } catch (IOException e) {
            connected.set(TaskStatus.BROKEN);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .taskId(config.getTaskId())
                    .replid(config.getReplId())
                    .msg(e.getMessage())
                    .build());
            log.warn("任务Id【{}】异常停止，停止原因【{}】", config.getTaskId(), "文件下载异常");
        }

        Objects.requireNonNull(in);
        Objects.requireNonNull(config);
        this.config = config;
        if (in instanceof PeekableInputStream) {
            this.peekable = (PeekableInputStream) in;
        } else {
            in = this.peekable = new PeekableInputStream(in);
        }
        this.inputStream = new RedisInputStream(in, this.config.getBufferSize());
//        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ProtocolReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();

    }

    @Override
    public void open() throws IOException {
        AtomicBoolean status = new AtomicBoolean(true);
        super.open();
        if (!compareAndSet(TaskStatus.BROKEN,TaskStatus.STOP, TaskStatus.STARTING)){
            return;
        }else {
            connected.set(TaskStatus.STARTING);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.STARTING)
                    .offset(config.getReplOffset())
                    .taskId(config.getTaskId())
                    .replid(config.getReplId())
                    .msg("RDB file task is starting")
                    .build());
        }
        try {
            doOpen();
        } catch (IOException | UnsupportedOperationException e) {
            status.set(false);
            connected.set(TaskStatus.BROKEN);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .taskId(config.getTaskId())
                    .replid(config.getReplId())
                    .msg(e.getMessage())
                    .build());
            log.error("IOException [{}] msg [{}]", e.getClass(), e.getMessage());
        } catch (IncrementException e) {
            status.set(false);
            connected.set(TaskStatus.BROKEN);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .taskId(config.getTaskId())
                    .replid(config.getReplId())
                    .msg(e.getMessage())
                    .build());
        } finally {
            doClose();
            if (status.get()) {
                if (handStop.get()) {
                    connected.set(TaskStatus.STOP);
                    doTaskStatusListener(this, SyncerTaskEvent
                            .builder()
                            .event(TaskStatus.STOP)
                            .offset(config.getReplOffset())
                            .taskId(config.getTaskId())
                            .replid(config.getReplId())
                            .msg("任务停止")
                            .build());
                }else {
                    connected.set(TaskStatus.FINISH);
                    doTaskStatusListener(this, SyncerTaskEvent
                            .builder()
                            .event(TaskStatus.FINISH)
                            .offset(config.getReplOffset())
                            .taskId(config.getTaskId())
                            .replid(config.getReplId())
                            .msg("任务完成")
                            .build());
                }
            }
        }
    }


    protected void doOpen() throws IOException, IncrementException {
        config.setReplOffset(0L);
        if (peekable.peek() == 'R') {
            RedisSyncerRdbParser parser = new RedisSyncerRdbParser(inputStream, this);
            connected.set(TaskStatus.RDBRUNNING);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.RDBRUNNING)
                    .offset(config.getReplOffset())
                    .replid(config.getReplId())
                    .taskId(config.getTaskId())
                    .msg("混合文件Rdb导入")
                    .build());
            config.setReplOffset(parser.parse());
        }
        if (getStatus() != TaskStatus.RDBRUNNING){
            return;
        }
        submitEvent(new PreCommandSyncEvent());
        connected.set(TaskStatus.COMMANDRUNNING);
        doTaskStatusListener(this, SyncerTaskEvent
                .builder()
                .event(TaskStatus.COMMANDRUNNING)
                .offset(config.getReplOffset())
                .replid(config.getReplId())
                .taskId(config.getTaskId())
                .msg("混合文件增量命令导入")
                .build());
        try {
            final long[] offset = new long[1];
            while (getStatus() == TaskStatus.COMMANDRUNNING) {
                Object obj = replyParser.parse(len -> offset[0] = len);
                if (obj instanceof Object[]) {
                    Object[] raw = (Object[]) obj;

                    CommandName name = CommandName.name(Strings.toString(raw[0]));
                    final CommandParser<? extends Command> parser;
                    if ((parser = commands.get(name)) == null) {
                        log.warn("command [{}] not register. raw command:{}", name, Strings.format(raw));
                        config.addOffset(offset[0]);
                        offset[0] = 0L;
                        continue;
                    }
                    final long st = config.getReplOffset();
                    final long ed = st + offset[0];
                    submitEvent(parser.parse(raw), Tuples.of(st, ed),config.getReplId(),ed);
                } else {
                    log.warn("unexpected redis reply:{}", obj);
                }
                config.addOffset(offset[0]);
                try {
                    config.setReadFileSize(config.getReplOffset());
                }catch (Exception e){

                }
                offset[0] = 0L;
            }
        } catch (EOFException ignore) {
            submitEvent(new PostCommandSyncEvent());
            config.setReadFileSize(config.getFileSize());
        }
    }

}

