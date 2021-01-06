package syncer.replica.replication;

import syncer.replica.entity.Configuration;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.SyncerEvent;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;
import syncer.replica.listener.DefaultExceptionListener;
import syncer.replica.net.NetStream;
import syncer.replica.rdb.RedisRdbParser;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Objects;

import static syncer.replica.entity.Status.*;

/**
 * @author zhanenqiang
 * @Description RDB解析
 * @Date 2020/8/10
 */
@Slf4j
public class RdbReplication extends AbstractReplication{
    public RdbReplication(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RdbReplication(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        if (configuration.isUseDefaultExceptionListener()) {
            addExceptionListener(new DefaultExceptionListener());
        }
    }


    public RdbReplication(String  filePath, Configuration configuration,boolean online) {
        try {
            InputStream in=null;
            //将在线和本地数据文件合并
            if(online){
                NetStream netStream=NetStream.builder().build();
                //得到输入流
                in = netStream.getInputStreamByOnlineFile(filePath);
            }else {
                in=new FileInputStream(filePath);
            }

            Objects.requireNonNull(in);
            Objects.requireNonNull(configuration);
            this.configuration = configuration;
            this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
            this.inputStream.setRawByteListeners(this.rawByteListeners);
            if (configuration.isUseDefaultExceptionListener()) {
                addExceptionListener(new DefaultExceptionListener());
            }
        }catch (Exception e){
            this.doTaskStatusListener(this, SyncerTaskEvent
                    .builder()
                    .taskStatusType(TaskStatusType.BROKEN)
                    .offset(configuration.getReplOffset())
                    .replid(configuration.getReplId())
                    .event(SyncerEvent.builder().taskId(configuration.getTaskId()).build())
                    .msg("RDB文件在线读取异常")
                    .build());
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
        try {
            new RedisRdbParser(inputStream, this).parse();
        } catch (EOFException ignore) {
        }
    }
}
