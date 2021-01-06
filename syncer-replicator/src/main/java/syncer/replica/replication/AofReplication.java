package syncer.replica.replication;

import syncer.replica.cmd.*;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.PostCommandSyncEvent;
import syncer.replica.event.PreCommandSyncEvent;
import syncer.replica.event.SyncerEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;
import syncer.replica.listener.DefaultExceptionListener;
import syncer.replica.net.NetStream;
import syncer.replica.util.objectutil.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

import static syncer.replica.entity.Status.*;
import static syncer.replica.util.objectutil.Strings.format;
import static syncer.replica.util.objectutil.Tuples.of;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
@Slf4j
public class AofReplication extends AbstractReplication {
    protected  ReplyParser replyParser;

    public AofReplication(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public AofReplication(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener()) {
            addExceptionListener(new DefaultExceptionListener());
        }
    }

    public AofReplication(String  filePath, Configuration configuration,boolean online){
        InputStream in=null;
        //将在线和本地数据文件合并
        try {
            if(online){
                NetStream netStream=NetStream.builder().build();
                //得到输入流
                in = netStream.getInputStreamByOnlineFile(filePath);
            }else {
                try{
                    in=new FileInputStream(filePath);
                } catch (FileNotFoundException e) {
                    doTaskStatusListener(this,SyncerTaskEvent
                            .builder()
                            .taskStatusType(TaskStatusType.BROKEN)
                            .offset(configuration.getReplOffset())
                            .replid(configuration.getReplId())
                            .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                            .msg("AOF文件不存在")
                            .build());
                }

            }

            Objects.requireNonNull(in);
            Objects.requireNonNull(configuration);
            this.configuration = configuration;
            this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
            this.inputStream.setRawByteListeners(this.rawByteListeners);
            this.replyParser = new ReplyParser(inputStream, new RedisCodec());
            builtInCommandParserRegister();
            if (configuration.isUseDefaultExceptionListener()) {
                addExceptionListener(new DefaultExceptionListener());
            }
        }catch (IOException e){
            this.doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("AOF文件在线读取异常")
                    .build());

            if(Objects.nonNull(in)){
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            log.error("[TASKID {}] 数据文件加载异常...",configuration.getTaskId());
            e.printStackTrace();
        }


    }

    @Override
    public void open() throws IOException {
        super.open();

        if (!compareAndSet(DISCONNECTED, CONNECTED)) {
            return;
        }
        boolean status=true;
        try {
            doOpen();
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)) {
                throw e.getCause();
            }
            status=false;
            this.doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("BROKEN")
                    .build());
        } catch (IncrementException e) {
            submitSyncerTaskEvent(SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg(e.getMessage()+"：Parser解析格式错误,请检查数据文件格式")
                    .build());
            status=false;
        } finally {
            doClose();
            doCloseListener(this);
            if(status){
                this.doTaskStatusListener(this, SyncerTaskEvent
                        .builder()
                        .taskStatusType(TaskStatusType.STOP)
                        .offset(configuration.getReplOffset())
                        .replid(configuration.getReplId())
                        .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                        .msg("STOP")
                        .build());
            }

        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.doTaskStatusListener(this, SyncerTaskEvent
                .builder()
                .taskStatusType(TaskStatusType.STOP)
                .offset(configuration.getReplOffset())
                .replid(configuration.getReplId())
                .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                .msg("STOP")
                .build());
    }

    protected void doOpen() throws IOException, IncrementException {
        configuration.setReplOffset(0L);
        submitEvent(new PreCommandSyncEvent());
        submitSyncerTaskEvent(SyncerTaskEvent
                .builder()
                .taskStatusType(TaskStatusType.COMMANDRUNING)
                .offset(configuration.getReplOffset())
                .replid(configuration.getReplId())
                .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                .msg("COMMANDRUNING")
                .build());
        try {
            final long[] offset = new long[1];
            while (getStatus() == CONNECTED) {
                Object obj = replyParser.parse(len -> offset[0] = len);
                if (obj instanceof Object[]) {
                    if (verbose() && log.isDebugEnabled())
                        log.debug(format((Object[]) obj));
                    Object[] raw = (Object[]) obj;
                    CommandName name = CommandName.name(Strings.toString(raw[0]));
                    final CommandParser<? extends Command> parser;
                    if ((parser = commands.get(name)) == null) {
                        log.warn("command [{}] not register. raw command:{}", name, format(raw));
                        configuration.addOffset(offset[0]);
                        offset[0] = 0L;
                        continue;
                    }
                    final long st = configuration.getReplOffset();
                    final long ed = st + offset[0];
                    submitEvent(parser.parse(raw), of(st, ed));
                } else {
                    log.warn("unexpected redis reply:{}", obj);
                }
                configuration.addOffset(offset[0]);
                offset[0] = 0L;
            }
            submitEvent(new PostCommandSyncEvent());
        } catch (EOFException ignore) {
            submitEvent(new PostCommandSyncEvent());
            submitSyncerTaskEvent(SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.STOP)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("STOP")
                    .build());
        }
    }
}
