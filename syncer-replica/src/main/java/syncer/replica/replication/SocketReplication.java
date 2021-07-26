package syncer.replica.replication;


import lombok.extern.slf4j.Slf4j;
import syncer.replica.cmd.CMD;
import syncer.replica.config.ReplicConfig;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.exception.RedisAuthErrorException;
import syncer.replica.heartbeat.Heartbeat;
import syncer.replica.io.RedisInputStream;
import syncer.replica.io.RedisOutputStream;
import syncer.replica.parser.protocol.ProtocolReplyParser;
import syncer.replica.protocol.DefaultSyncRedisProtocol;
import syncer.replica.retry.SocketReplicationRetrier;
import syncer.replica.socket.RedisSocketFactory;
import syncer.replica.status.TaskStatus;
import syncer.replica.util.code.RedisCodec;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.CapaSyncType;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
@Slf4j
public class SocketReplication  extends AbstractReplication{
    private int port;
    private String host;
    private int db = -1;
    private Socket socket;
    private ProtocolReplyParser replyParser;
    private Heartbeat heartbeat;
    private RedisOutputStream outputStream;
    protected RedisSocketFactory socketFactory;
    protected DefaultSyncRedisProtocol syncRedisProtocol;
    private SocketReplicationRetrier socketReplicationRetrier;
    /**
     * sentinel failover 结束
     *      * 是否属于哨兵模式-》哨兵模式不会进入break 而是 failover
     *      * 是 true
     *      * 否 false
     */
    protected boolean sentinelFailover=false;

    /**
     * full resync 是否继续或者结束
     * true   继续
     * false  结束
     */
    private  boolean status=true;

    public static final String REPLY_ERROR_MSG="Can't SYNC while not connected with my master";
    private AtomicInteger count = new AtomicInteger();

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public ReplicConfig getConfig() {
        return config;
    }
    public SocketReplication(String host, int port, ReplicConfig config, boolean status){
        this(host,port,config,status,false);
    }

    public SocketReplication(String host, int port, ReplicConfig config, boolean status,boolean sentinelFailover) {
        Objects.requireNonNull(host);
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("[TASKID "+config.getTaskId()+"]illegal argument port: " + port);
        }
        Objects.requireNonNull(config);
        this.host = host;
        this.port = port;
        this.status = status;
        this.config = config;
        this.sentinelFailover= sentinelFailover;
        this.socketFactory = new RedisSocketFactory(config);
        this.syncRedisProtocol= DefaultSyncRedisProtocol.builder().socket(socket)
                .configuration(config)
                .heartbeat(new Heartbeat())
                .replication(this)
                .replyParser(replyParser)
                .outputStream(outputStream).build();

    }

    @Override
    public void open() throws IOException {
        AtomicBoolean status=new AtomicBoolean(true);
        super.open();
        try {
            brokenMSg="";
            socketReplicationRetrier=new SocketReplicationRetrier(syncRedisProtocol,this,config,replyParser);
            socketReplicationRetrier.retry(this);
        } catch (IncrementException  e) {
            connected.set(TaskStatus.BROKEN);
            status.set(false);
            log.error("TASKID[{}] error reason {}",config.getTaskId(),e.getMessage());
            syncRedisProtocol.getReplication().doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .taskId(config.getTaskId())
                    .replid(config.getReplId())
                    .msg(e.getMessage())
                    .build());
            brokenMSg=e.getMessage();
        }catch (Exception e){
//            e.printStackTrace();
            log.error("Exception {} ,msg {}",e.getClass(),e.getMessage());
        } finally {
            if(status.get()){
                if(handStop.get()){
                    if(sentinelFailover){
                        connected.set(TaskStatus.FAILOVER);
                        syncRedisProtocol.getReplication().doTaskStatusListener(this, SyncerTaskEvent
                                .builder()
                                .event(TaskStatus.FAILOVER)
                                .offset(config.getReplOffset())
                                .taskId(config.getTaskId())
                                .replid(config.getReplId())
                                .msg("哨兵故障转移状态...")
                                .build());
                    }else {
                        connected.set(TaskStatus.STOP);
                        syncRedisProtocol.getReplication().doTaskStatusListener(this, SyncerTaskEvent
                                .builder()
                                .event(TaskStatus.STOP)
                                .offset(config.getReplOffset())
                                .replid(config.getReplId())
                                .taskId(config.getTaskId())
                                .msg("任务停止")
                                .build());
                    }

                }else {
                    connected.set(TaskStatus.BROKEN);
                    SyncerTaskEvent event= SyncerTaskEvent
                            .builder()
                            .event(TaskStatus.BROKEN)
                            .offset(config.getReplOffset())
                            .taskId(config.getTaskId())
                            .replid(config.getReplId())
                            .msg("重试多次后失败->"+brokenMSg)
                            .build();

                    if(!Strings.isEquals("",brokenMSg)&&brokenMSg!=null){
                        event.setMsg(brokenMSg);
                    }
                    syncRedisProtocol.getReplication().doTaskStatusListener(this, event);
//                    if(sentinelFailover){
//                        connected.set(TaskStatus.FAILOVER);
//                        syncRedisProtocol.getReplication().doTaskStatusListener(this, SyncerTaskEvent
//                                .builder()
//                                .event(TaskStatus.FAILOVER)
//                                .offset(config.getReplOffset())
//                                .taskId(config.getTaskId())
//                                .replid(config.getReplId())
//                                .msg("哨兵故障转移状态...")
//                                .build());
//                    }else {
//                        connected.set(TaskStatus.BROKEN);
//                        syncRedisProtocol.getReplication().doTaskStatusListener(this, SyncerTaskEvent
//                                .builder()
//                                .event(TaskStatus.BROKEN)
//                                .offset(config.getReplOffset())
//                                .taskId(config.getTaskId())
//                                .replid(config.getReplId())
//                                .msg("重试多次后失败")
//                                .build());
//                    }

                }


            }else{
                if(!handStop.get()){
                    SyncerTaskEvent event= SyncerTaskEvent
                            .builder()
                            .event(TaskStatus.BROKEN)
                            .offset(config.getReplOffset())
                            .taskId(config.getTaskId())
                            .replid(config.getReplId())
                            .msg("重试多次后失败->"+brokenMSg)
                            .build();
                    if(!Strings.isEquals("",brokenMSg)&&brokenMSg!=null){
                        event.setMsg(brokenMSg);
                    }
                    syncRedisProtocol.getReplication().doTaskStatusListener(this, event);
                }
            }
            doClose();

        }
    }



    @Override
    public void doClose() throws IOException {
        super.doClose();
        if(heartbeat!=null){
            heartbeat.close();
        }
        if(outputStream!=null){
            outputStream.close();
        }
    }



    public CapaSyncType trySync(final String reply) throws IOException, IncrementException, RedisAuthErrorException {
        log.info("[TASKID {}] response : {}",config.getTaskId(),reply);
        if(reply.contains(REPLY_ERROR_MSG)){
            if(count.get()>=3){
                close();
                throw new IncrementException("NOMASTERLINK Can't SYNC while not connected with my master");
            }
            count.incrementAndGet();
        }
        //全量开始  +FULLRESYNC repl_id repl_offset\r\n
        if (reply.startsWith(CMD.FULL_RESYNC)) {
            //断点续传 offset刷过时
            if(!status){
                throw new IncrementException("增量同步runId不存在..结束,[请检查offset是否刷过/或者当前任务之前未进行过数据同步但afresh设置为false]");
            }
            // reset db
            this.db = -1;
            syncRedisProtocol.parseDump(this);
            String[] ary = reply.split(" ");
            config.setReplId(ary[1]);
            config.setReplOffset(Long.parseLong(ary[2]));
            return CapaSyncType.PSYNC;
        } else if (reply.startsWith(CMD.CONTINUE)) {
            // +CONTINUE\r\n
            String[] ary = reply.split(" ");
            // redis-4.0 compatible
            String replId = config.getReplId();
            if (ary.length > 1 && replId != null && !replId.equals(ary[1])){
                config.setReplId(ary[1]);
            }
            return CapaSyncType.PSYNC;
        } else if (reply.startsWith(CMD.NOMASTERLINK) || reply.startsWith(CMD.LOADING)) {
            return CapaSyncType.SYNC_LATER;
        } else {
            //SYNC
            log.info("[TASKID {}] {}",config.getTaskId(),CMD.SYNC);
            syncRedisProtocol.send(CMD.SYNC.getBytes());
            // reset db
            this.db = -1;
            syncRedisProtocol.parseDump(this);
            return CapaSyncType.SYNC;
        }

    }



    public boolean isRunning(){
        return TaskStatus.RDBRUNNING.equals(getStatus())||TaskStatus.COMMANDRUNNING.equals(getStatus())||TaskStatus.STARTING.equals(getStatus());
    }

    @Override
    public boolean isClosed() {
        return super.isClosed();
    }

    /**
     * 建立连接
     * 发送同步命令
     * @throws IOException
     * @throws IncrementException
     * @throws RedisAuthErrorException
     */
    public void establishConnection() throws IOException, IncrementException, RedisAuthErrorException {
        connect();
        if (Objects.nonNull(config.getAuthPassword())) {
            syncRedisProtocol.auth(config.getAuthUser(), config.getAuthPassword());
        }
        syncRedisProtocol.ping();
        syncRedisProtocol.sendSlaveListeningPort();
        syncRedisProtocol.sendSlaveIpAddress();
        syncRedisProtocol.sendCapa(CMD.EOF);
        syncRedisProtocol.sendCapa(CMD.PSYNC2);
    }

    /**
     * 创建socket
     */
    private void connect() throws IOException {
        if (!compareAndSet(TaskStatus.BROKEN,TaskStatus.STOP, TaskStatus.STARTING)) {
            return;
        }
        try {
            socket = socketFactory.createSocket(host, port, config.getConnectionTimeout());
            loadSocket(socket);
            outputStream = new RedisOutputStream(socket.getOutputStream());
            loadOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            this.inputStream = new RedisInputStream(inputStream, config.getBufferSize());
            this.inputStream.setRawByteListenerList(this.rawByteListenerList);
            replyParser =new ProtocolReplyParser(this.inputStream, new RedisCodec());
            loadReplyParser(replyParser);
            log.info("[TASKID {}] Connected to redis-server[{}:{}]", config.getTaskId(),host, port);
        }finally {
            setStatus(TaskStatus.STARTING);
        }
    }

    /**
     * 装载输出流
     * @param outputStream
     */
    private void loadOutputStream(RedisOutputStream outputStream) {
        syncRedisProtocol.setOutputStream(outputStream);
    }

    /**
     * 装载socket
     * @param socket
     */
    void loadSocket(Socket socket){
        syncRedisProtocol.setSocket(socket);
    }


    /**
     * 装载 RESP解析器
     * @param replyParser
     */
    void loadReplyParser(ProtocolReplyParser replyParser){
        syncRedisProtocol.setReplyParser(replyParser);
        socketReplicationRetrier.setReplyParser(replyParser);
    }

}
