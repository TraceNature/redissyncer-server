package syncer.replica.replication;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.config.ReplicConfig;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;
import syncer.replica.parser.RedisSyncerRdbParser;
import syncer.replica.protocol.DefaultSyncRedisProtocol;
import syncer.replica.socket.NetStream;
import syncer.replica.status.TaskStatus;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RDB 文件导入
 */
@Slf4j
public class RdbReplication extends AbstractReplication{

    public RdbReplication(File file, ReplicConfig configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RdbReplication(InputStream in, ReplicConfig configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.config = configuration;
        this.inputStream = new RedisInputStream(in, this.config.getBufferSize());
        this.inputStream.setRawByteListenerList(this.rawByteListenerList);
    }


    public RdbReplication(String filePath, ReplicConfig config) throws IOException {
        this(filePath,config,false);
    }
    public RdbReplication(String filePath, ReplicConfig config,boolean online) throws IOException {
        InputStream in = null;
        try {
            if(online){
                NetStream netStream= NetStream.builder().build();

                in=netStream.getInputStreamByOnlineFile(filePath);

            }else {
                in = new FileInputStream(filePath);
            }
        } catch (FileNotFoundException e) {
            connected.set(TaskStatus.BROKEN);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .replid(config.getReplId())
                    .taskId(config.getTaskId())
                    .msg(e.getMessage())
                    .build());
            log.warn("任务Id【{}】异常停止，停止原因【{}】", config.getTaskId(), "本地文件未找到");
            throw e;
        } catch (SSLHandshakeException e){
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .replid(config.getReplId())
                    .taskId(config.getTaskId())
                    .msg(e.getMessage())
                    .build());
            log.warn("任务Id【{}】异常停止，停止原因【{}】", config.getTaskId(), "文件下载异常:"+e.getMessage());
            throw e;
        } catch (IOException e) {
            connected.set(TaskStatus.BROKEN);
            doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .event(TaskStatus.BROKEN)
                    .offset(config.getReplOffset())
                    .replid(config.getReplId())
                    .taskId(config.getTaskId())
                    .msg(e.getMessage())
                    .build());
            log.warn("任务Id【{}】异常停止，停止原因【{}】", config.getTaskId(), "文件下载异常");
            throw e;
        }

        Objects.requireNonNull(in);
        Objects.requireNonNull(config);
        this.config = config;
        this.inputStream = new RedisInputStream(in, this.config.getBufferSize());
    }


    @Override
    public void open() throws IOException {
        AtomicBoolean status = new AtomicBoolean(true);
        super.open();
        if (!compareAndSet(TaskStatus.BROKEN, TaskStatus.STOP, TaskStatus.STARTING)) {
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
        } catch (IOException  | UnsupportedOperationException e) {
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
                            .replid(config.getReplId())
                            .taskId(config.getTaskId())
                            .msg("任务停止")
                            .build());
                }else {
                    connected.set(TaskStatus.FINISH);
                    doTaskStatusListener(this, SyncerTaskEvent
                            .builder()
                            .event(TaskStatus.FINISH)
                            .offset(config.getReplOffset())
                            .replid(config.getReplId())
                            .taskId(config.getTaskId())
                            .msg("任务完成")
                            .build());
                }
            }
        }
    }

    protected void doOpen() throws IOException,IncrementException {
        try {
            new RedisSyncerRdbParser(inputStream, this).parse();
        } catch (EOFException ignore) {
            throw ignore;
        }
    }

}
