package syncer.replica.replication;

import syncer.replica.cmd.RedisCodec;
import syncer.replica.cmd.ReplyParser;
import syncer.replica.cmd.synccomand.SocketSyncCommand;
import syncer.replica.constant.CMD;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.SyncMode;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.SyncerEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.AsyncBufferedInputStream;
import syncer.replica.io.RateLimitInputStream;
import syncer.replica.io.RedisInputStream;
import syncer.replica.io.RedisOutputStream;
import syncer.replica.net.RedisSocketFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static syncer.replica.entity.Status.*;
import static syncer.replica.entity.SyncMode.*;
import static syncer.replica.util.thread.ConcurrentUtils.terminateQuietly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
@Slf4j
@Getter
@Setter
public class SocketReplication extends AbstractReplication{
    protected  int port;
    protected  String host;
    public int db = -1;
    protected Socket socket;
    protected ReplyParser replyParser;
    protected ScheduledFuture<?> heartbeat;
    protected RedisOutputStream outputStream;
    //心跳检测线程
    protected ScheduledExecutorService executor;
    protected RedisSocketFactory socketFactory;

    protected SocketSyncCommand socketSyncCommand;
    private SocketReplicationRetrier socketReplicationRetrier;

    /**
     * full resync 是否继续或者结束
     * true   继续
     * false  结束
     */
    protected  boolean status=true;

    public static final String REPLY_ERROR_MSG="Can't SYNC while not connected with my master";
    private AtomicInteger count = new AtomicInteger();

    public SocketReplication(String host, int port, Configuration configuration,boolean status) {
        Objects.requireNonNull(host);
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("[TASKID "+configuration.getTaskId()+"]illegal argument port: " + port);
        }
        Objects.requireNonNull(configuration);
        this.host = host;
        this.port = port;
        this.status = status;
        this.configuration = configuration;
        this.socketFactory = new RedisSocketFactory(configuration);

        socketSyncCommand=SocketSyncCommand.builder().socket(socket)
                .configuration(configuration)
                .executor(executor)
                .replication(this)
                .replyParser(replyParser)
                .outputStream(outputStream).build();

    }

    @Override
    public void open() throws IOException {
        super.open();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        loadExecutor(executor);
        try {
            socketReplicationRetrier=new SocketReplicationRetrier(socketSyncCommand,this,configuration,replyParser);
            socketReplicationRetrier.retry(this);

        } finally {
            socketSyncCommand.getReplication().doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("重试多次后失败")
                    .build());
            doClose();
            doCloseListener(this);
            terminateQuietly(executor, configuration.getConnectionTimeout(), MILLISECONDS);
        }

    }


    void loadSocket(Socket socket){
        socketSyncCommand.setSocket(socket);
    }
    void loadOutputStream(RedisOutputStream outputStream){
        socketSyncCommand.setOutputStream(outputStream);
    }

    void loadReplyParser(ReplyParser replyParser){
        socketSyncCommand.setReplyParser(replyParser);
        socketReplicationRetrier.setReplyParser(replyParser);
        socketReplicationRetrier.setReplyParser(replyParser);
    }

    void loadExecutor(ScheduledExecutorService executor){
        socketSyncCommand.setExecutor(executor);
    }



    protected void connect() throws IOException {
        if (!compareAndSet(DISCONNECTED, CONNECTING)) {
            return;
        }
        try {
            socket = socketFactory.createSocket(host, port, configuration.getConnectionTimeout());
            loadSocket(socket);
            outputStream = new RedisOutputStream(socket.getOutputStream());
            loadOutputStream(outputStream);

            InputStream inputStream = socket.getInputStream();
            if (configuration.getAsyncCachedBytes() > 0) {
                inputStream = new AsyncBufferedInputStream(inputStream, configuration.getAsyncCachedBytes());
            }
            if (configuration.getRateLimit() > 0) {
                inputStream = new RateLimitInputStream(inputStream, configuration.getRateLimit());
            }
            this.inputStream = new RedisInputStream(inputStream, configuration.getBufferSize());
            this.inputStream.setRawByteListeners(this.rawByteListeners);
            replyParser = new ReplyParser(this.inputStream, new RedisCodec());
            loadReplyParser(replyParser);
            log.info("[TASKID {}] Connected to redis-server[{}:{}]", configuration.getTaskId(),host, port);
        } finally {
            setStatus(CONNECTED);
        }
    }


    /**
     * 建立链接
     * @throws IOException
     */
    protected void establishConnection() throws IOException, IncrementException {
        connect();
        if (configuration.getAuthPassword() != null) {
            socketSyncCommand.auth(configuration.getAuthUser(), configuration.getAuthPassword());
        }
        socketSyncCommand.sendPing();
        socketSyncCommand.sendSlavePort();
        socketSyncCommand.sendSlaveIp();
        socketSyncCommand.sendSlaveCapa(CMD.EOF);
        socketSyncCommand.sendSlaveCapa(CMD.PSYNC2);
    }


    /**
     * FULLRESYNC 35647733d1100fb8f781ea5b760b5293aadacc9f 1756901953
     * CONTINUE
     * NOMASTERLINK
     * LOADING
     * SYNC
     * @param reply
     * @return
     * @throws IOException
     */
    public SyncMode trySync(final String reply) throws IOException, IncrementException {
        log.info("[TASKID {}] {}",configuration.getTaskId(),reply);
        if(reply.contains(REPLY_ERROR_MSG)){
            if(count.get()>=3){
                close();
                throw new IncrementException("NOMASTERLINK Can't SYNC while not connected with my master");

            }
            count.incrementAndGet();
        }

        if (reply.startsWith(CMD.FULL_RESYNC)) {
            if(!status){
                throw new IncrementException("增量同步runId不存在..结束,[请检查offset是否刷过/或者当前任务之前未进行过数据同步但afresh设置为false]");
            }
            // reset db
            this.db = -1;
            socketSyncCommand.parseDump(this);
            String[] ary = reply.split(" ");
            configuration.setReplId(ary[1]);
            configuration.setReplOffset(Long.parseLong(ary[2]));
            return PSYNC;
        } else if (reply.startsWith(CMD.CONTINUE)) {
            String[] ary = reply.split(" ");
            // redis-4.0 compatible
            String replId = configuration.getReplId();
            if (ary.length > 1 && replId != null && !replId.equals(ary[1])){
                configuration.setReplId(ary[1]);
            }
            return PSYNC;
        } else if (reply.startsWith(CMD.NOMASTERLINK) || reply.startsWith(CMD.LOADING)) {
            return SYNC_LATER;
        } else {
            log.info("[TASKID {}] {}",configuration.getTaskId(),CMD.SYNC);
            socketSyncCommand.send(CMD.SYNC.getBytes());
            // reset db
            this.db = -1;
            socketSyncCommand.parseDump(this);
            return SYNC;
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

    @Override
    protected boolean isClosed() {
        return super.isClosed();
    }

    @Override
    protected void doClose() throws IOException {
        compareAndSet(CONNECTED, DISCONNECTING);

        try {
            if (heartbeat != null) {
                if (!heartbeat.isCancelled()) {
                    heartbeat.cancel(true);
                }
                log.info("[TASKID {}] heartbeat canceled.",configuration.getTaskId());
            }

            try {
                if (inputStream != null) {
                    inputStream.setRawByteListeners(null);
                    inputStream.close();
                }
            } catch (IOException e) {
                // NOP
            }

            try {
                if (outputStream != null){
                    outputStream.close();
                }
            } catch (IOException e) {
                // NOP
            }
            try {
                if (socket != null && !socket.isClosed()){
                    socket.close();
                }
            } catch (IOException e) {
                // NOP
            }
            log.info("[TASKID {}] socket closed. redis-server[{}:{}]",configuration.getTaskId(), host, port);
        } finally {
            setStatus(DISCONNECTED);
        }
    }
}
