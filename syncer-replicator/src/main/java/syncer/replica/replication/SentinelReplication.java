package syncer.replica.replication;

import syncer.jedis.HostAndPort;
import syncer.replica.cmd.Command;
import syncer.replica.cmd.CommandName;
import syncer.replica.cmd.CommandParser;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.Status;
import syncer.replica.listener.*;
import syncer.replica.rdb.RedisRdbVisitor;
import syncer.replica.rdb.datatype.Module;
import syncer.replica.rdb.module.ModuleParser;
import syncer.replica.sentinel.Sentinel;
import syncer.replica.sentinel.SentinelListener;
import syncer.replica.sentinel.impl.SyncerSentinel;
import syncer.replica.util.Reflections;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static syncer.replica.util.thread.ConcurrentUtils.terminateQuietly;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author zhanenqiang
 * @Description 哨兵模式支持源的failover
 * @Date 2020/8/14
 */
@Slf4j
public class SentinelReplication implements Replication, SentinelListener {
    private HostAndPort prev;
    private  Sentinel sentinel;
    private  SocketReplication replication;
    protected final ExecutorService executors = newSingleThreadExecutor();

    public SentinelReplication(List<HostAndPort> hosts, String name, Configuration configuration) {
        Objects.requireNonNull(hosts);
        Objects.requireNonNull(configuration);
        this.replication = new SocketReplication("", 1, configuration,true);
        this.sentinel = new SyncerSentinel(hosts, name, configuration);

        this.sentinel.addSentinelListener(this);
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
    public boolean addEventListener(EventListener listener) {
        return replication.addEventListener(listener);
    }

    @Override
    public boolean removeEventListener(EventListener listener) {
        return replication.removeEventListener(listener);
    }

    @Override
    public boolean addRawByteListener(RawByteListener listener) {
        return replication.addRawByteListener(listener);
    }

    @Override
    public boolean removeRawByteListener(RawByteListener listener) {
        return replication.removeRawByteListener(listener);
    }

    @Override
    public boolean addCloseListener(CloseListener listener) {
        return replication.addCloseListener(listener);
    }

    @Override
    public boolean removeCloseListener(CloseListener listener) {
        return replication.removeCloseListener(listener);
    }

    @Override
    public boolean addExceptionListener(ExceptionListener listener) {
        return replication.addExceptionListener(listener);
    }

    @Override
    public boolean removeExceptionListener(ExceptionListener listener) {
        return replication.removeExceptionListener(listener);
    }

    @Override
    public boolean addStatusListener(StatusListener listener) {
        return replication.addStatusListener(listener);
    }

    @Override
    public boolean removeStatusListener(StatusListener listener) {
        return replication.removeStatusListener(listener);
    }

    @Override
    public boolean addTaskStatusListener(TaskStatusListener listener) {
        return replication.addTaskStatusListener(listener);
    }

    @Override
    public boolean removeTaskStatusListener(TaskStatusListener listener) {
        return replication.addTaskStatusListener(listener);
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
    public void setRdbVisitor(RedisRdbVisitor rdbVisitor) {
        replication.setRdbVisitor(rdbVisitor);
    }

    @Override
    public RedisRdbVisitor getRdbVisitor() {
        return replication.getRdbVisitor();
    }

    @Override
    public boolean verbose() {
        return replication.verbose();
    }

    @Override
    public Status getStatus() {
        return replication.getStatus();
    }

    @Override
    public Configuration getConfiguration() {
        return replication.getConfiguration();
    }

    @Override
    public void onSwitch(Sentinel sentinel, HostAndPort next) {
        if (prev == null || !prev.equals(next)) {
            log.info("Sentinel switch master to [{}]", next);
            CommonReplications.closeQuietly(replication);
            executors.submit(() -> {
                Reflections.setField(replication, "host", next.getHost());
                Reflections.setField(replication, "port", next.getPort());
                CommonReplications.openQuietly(replication);
            });
        }
        prev = next;
    }

    @Override
    public void onClose(Sentinel sentinel) {
        CommonReplications.closeQuietly(replication);
        terminateQuietly(executors, getConfiguration().getConnectionTimeout(), MILLISECONDS);
    }


}
