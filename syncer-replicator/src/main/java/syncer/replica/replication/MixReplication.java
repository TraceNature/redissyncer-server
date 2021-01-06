package syncer.replica.replication;

import syncer.replica.cmd.*;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.PostCommandSyncEvent;
import syncer.replica.event.PreCommandSyncEvent;
import syncer.replica.event.SyncerEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.PeekableInputStream;
import syncer.replica.io.RedisInputStream;
import syncer.replica.listener.DefaultExceptionListener;
import syncer.replica.net.NetStream;
import syncer.replica.rdb.RedisRdbParser;
import syncer.replica.util.objectutil.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

import static syncer.replica.entity.Status.*;
import static syncer.replica.util.objectutil.Strings.format;
import static syncer.replica.util.objectutil.Tuples.of;

/**
 * @author zhanenqiang
 * @Description 混合文件
 * @Date 2020/8/10
 */
@Slf4j
public class MixReplication extends AbstractReplication {
    protected ReplyParser replyParser;
    protected PeekableInputStream peekable;


    public MixReplication(File file, Configuration configuration) throws FileNotFoundException {

        this(new FileInputStream(file), configuration);
    }

    public MixReplication(String fileUrl, Configuration configuration,boolean online) throws FileNotFoundException {
        InputStream in = null;
        try {
            //将在线和本地数据文件合并
            if(online){
                NetStream netStream=NetStream.builder().build();
                //得到输入流
                in = netStream.getInputStreamByOnlineFile(fileUrl);
            }else {
                in=new FileInputStream(fileUrl);
            }
            Objects.requireNonNull(in);
            Objects.requireNonNull(configuration);
            this.configuration = configuration;
            if (in instanceof PeekableInputStream) {
                this.peekable = (PeekableInputStream) in;
            } else {
                in = this.peekable = new PeekableInputStream(in);
            }
            this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
            this.inputStream.setRawByteListeners(this.rawByteListeners);
            this.replyParser = new ReplyParser(inputStream, new RedisCodec());
            builtInCommandParserRegister();
            if (configuration.isUseDefaultExceptionListener()) {
                addExceptionListener(new DefaultExceptionListener());
            }
        } catch (Exception e) {

            this.doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("mix文件在线读取异常")
                    .build());
            log.error("[TASKID {}] mix数据文件加载异常...",configuration.getTaskId());

            e.printStackTrace();
        }

    }


    public MixReplication(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        if (in instanceof PeekableInputStream) {
            this.peekable = (PeekableInputStream) in;
        } else {
            in = this.peekable = new PeekableInputStream(in);
        }
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener()) {
            addExceptionListener(new DefaultExceptionListener());
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
            this.doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("BROKEN")
                    .build());
            status=false;
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
        if (peekable.peek() == 'R') {
            RedisRdbParser parser = new RedisRdbParser(inputStream, this);
            configuration.setReplOffset(parser.parse());
        }
        if (getStatus() != CONNECTED) {
            return;
        }
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
                    if (verbose() && log.isDebugEnabled()) {
                        log.debug(format((Object[]) obj));
                    }
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
