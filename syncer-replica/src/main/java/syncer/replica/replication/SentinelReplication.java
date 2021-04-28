package syncer.replica.replication;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.jedis.HostAndPort;
import syncer.jedis.util.IOUtils;
import syncer.replica.config.ReplicConfig;
import syncer.replica.datatype.command.Command;
import syncer.replica.datatype.command.CommandName;
import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskRawByteListener;
import syncer.replica.listener.TaskStatusListener;
import syncer.replica.parser.IRdbParser;
import syncer.replica.sentinel.Reflections;
import syncer.replica.sentinel.Sentinel;
import syncer.replica.sentinel.SentinelListener;
import syncer.replica.sentinel.SyncerRedisSentinel;
import syncer.replica.status.TaskStatus;
import syncer.replica.util.OffsetPlace;
import syncer.replica.util.SyncTypeUtils;
import syncer.replica.util.redis.RedisReplId;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;


@Slf4j
public class SentinelReplication implements Replication, SentinelListener{
    private HostAndPort prev;
    private final Sentinel sentinel;
    private final SocketReplication replication;
    protected final ExecutorService executors = newSingleThreadExecutor();
    private AtomicInteger failoverNum=new AtomicInteger(0);

    RedisReplId redisReplIdCheck=new RedisReplId();
    /**
     * full resync 是否继续或者结束
     * true   继续
     * false  结束
     */
    private  boolean status=true;

    public SentinelReplication(List<HostAndPort> hosts, String name, ReplicConfig config,boolean status) {
        Objects.requireNonNull(hosts);
        Objects.requireNonNull(config);
        this.replication = new SocketReplication("", 1, config,status,true);
        this.sentinel = new SyncerRedisSentinel(hosts, name, config,this);
        this.sentinel.addSentinelListener(this);
    }




    @Override
    public boolean addEventListener(EventListener listener) {
        return replication.addEventListener(listener);
    }

    @Override
    public boolean removeEventListener(EventListener listener) {
        return replication.removeEventListener(listener);
    }

    @Override
    public boolean addRawByteListener(TaskRawByteListener listener) {
        return replication.addRawByteListener(listener);
    }

    @Override
    public boolean removeRawByteListener(TaskRawByteListener listener) {
        return replication.removeRawByteListener(listener);
    }

    @Override
    public boolean addTaskStatusListener(TaskStatusListener listener) {
        return replication.addTaskStatusListener(listener);
    }

    @Override
    public boolean removeTaskStatusListener(TaskStatusListener listener) {
        return replication.removeTaskStatusListener(listener);
    }

    @Override
    public void builtInCommandParserRegister() {
        replication.builtInCommandParserRegister();
    }

    @Override
    public CommandParser<? extends Command> getCommandParser(CommandName command) {
        return replication.getCommandParser(command);
    }

    @Override
    public <T extends Command> void addCommandParser(CommandName command, CommandParser<T> parser) {
        replication.addCommandParser(command, parser);
    }

    @Override
    public CommandParser<? extends Command> removeCommandParser(CommandName command) {
        return replication.removeCommandParser(command);
    }

    @Override
    public ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion) {
        return replication.getModuleParser(moduleName, moduleVersion);
    }

    @Override
    public <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser) {
        replication.addModuleParser(moduleName, moduleVersion, parser);
    }

    @Override
    public ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion) {
        return replication.removeModuleParser(moduleName, moduleVersion);
    }

    @Override
    public void setRdbParser(IRdbParser rdbParser) {
        replication.setRdbParser(rdbParser);
    }

    @Override
    public IRdbParser getRdbVisitor() {
        return replication.getRdbVisitor();
    }

    @Override
    public TaskStatus getStatus() {
        return replication.getStatus();
    }

    @Override
    public ReplicConfig getConfig() {
        return replication.getConfig();
    }

    @Override
    public void open() throws IOException {
        this.sentinel.open();

    }

    @Override
    public void close() throws IOException {
        this.sentinel.close();
    }

    @Override
    public void broken(String reason) throws IOException {
        replication.broken(reason);
    }


    @Override
    public void onSwitch(Sentinel sentinel, HostAndPort next) {
        if (prev == null || !prev.equals(next)) {
            log.info("TASKID[{}]Sentinel switch master to [{}]", getConfig().getTaskId(),next);
            closeQuietly(replication);
            if(failoverNum.getAndIncrement()>0){
                String[] data = new String[0];
                try {
                    data = redisReplIdCheck.selectSyncerBuffer(next.getHost(), next.getPort(),getConfig().getAuthUser(),getConfig().getAuthPassword(), SyncTypeUtils.getOffsetPlace(OffsetPlace.ENDBUFFER.getCode()).getOffsetPlace());
                    if(data[1]==null){
                        try {
                            replication.broken("故障转移失败...replid获取失败");
                            onClose(sentinel);
                            close();
                            log.error("TASKID [] 故障转移失败...replid获取失败0");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                } catch (URISyntaxException e) {
                    try {
                        replication.broken("故障转移失败...replid获取失败");
                        onClose(sentinel);
                        close();
                        log.error("TASKID [] 故障转移失败...replid获取失败1");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                }

                String replid=data[1];
                replication.setStatus(false);
                getConfig().setReplId(replid);
                long offsetNum = Long.parseLong(data[0]);
//                long offsetNum =getConfig().getReplOffset();
                offsetNum -= 1;
//                getConfig().setReplOffset(getConfig().getReplOffset()-1);
                getConfig().setReplOffset(offsetNum);
            }
            executors.submit(() -> {
                Reflections.setField(replication, "host", next.getHost());
                Reflections.setField(replication, "port", next.getPort());
                openQuietly(replication);
            });
        }
        prev = next;
    }

    @Override
    public void onClose(Sentinel sentinel) {
        closeQuietly(replication);
        IOUtils.terminateQuietly(executors, getConfig().getConnectionTimeout(), MILLISECONDS);
    }


    public void closeQuietly(Replication replication) {
        try {
            Objects.requireNonNull(replication).close();
        } catch (Exception e) {

        }
    }

    /*
     * SYNC
     */
    public void openQuietly(Replication repliction) {
        try {
            Objects.requireNonNull(repliction).open();
        } catch (Exception e) {

        }
    }

}
