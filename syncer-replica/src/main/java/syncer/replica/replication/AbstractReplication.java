package syncer.replica.replication;

import syncer.replica.config.ReplicConfig;
import syncer.replica.datatype.command.Command;
import syncer.replica.datatype.command.CommandName;
import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.datatype.rdb.module.ModuleKey;
import syncer.replica.event.Event;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.io.RedisInputStream;
import syncer.replica.kv.AbstractEvent;
import syncer.replica.listener.AbstractReplicationListener;
import syncer.replica.parser.DefaultRedisRdbParser;
import syncer.replica.parser.IRdbParser;
import syncer.replica.status.TaskStatus;
import syncer.replica.util.tuple.Tuple2;
import syncer.replica.util.tuple.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public  class AbstractReplication extends AbstractReplicationListener implements Replication {
    protected ReplicConfig config;
    protected RedisInputStream inputStream;
    private final AtomicBoolean manual = new AtomicBoolean(false);
    protected AtomicBoolean handStop =new AtomicBoolean(false);
    protected String brokenMSg="";
    /**
     * 状态
     */
    protected final AtomicReference<TaskStatus> connected = new AtomicReference<>(TaskStatus.STOP);
    /**
     * 增量命令
     */
    protected final Map<CommandName, CommandParser<? extends Command>> commands = new ConcurrentHashMap<>();


    private final Map<ModuleKey, ModuleParser<? extends Module>> modules = new ConcurrentHashMap<>();

    private IRdbParser rdbParser = new DefaultRedisRdbParser(this);

    @Override
    public void open() throws IOException {
        resetBrokenReason();
        manual.compareAndSet(true, false);
    }

    /**
     * 重置异常信息
     */
    void resetBrokenReason(){
        brokenMSg="";
    }
    @Override
    public void close() throws IOException {
        handStop.compareAndSet(handStop.get(),true);
        compareAndSet(connected.get(), TaskStatus.STOP);
        manual.compareAndSet(false, true);
    }

    @Override
    public void broken(String reason) throws IOException {
        compareAndSet(connected.get(), TaskStatus.BROKEN);
        manual.compareAndSet(false, true);
        handStop.set(false);
        resetBrokenReason();
    }


    public AtomicReference<TaskStatus> getConnected() {
        return connected;
    }

    public AtomicBoolean getManual() {
        return manual;
    }

    protected boolean isClosed() {
        return manual.get();
    }


    public void doClose() throws IOException {
        compareAndSet(connected.get(), TaskStatus.STOP);
        try {
            if(Objects.nonNull(inputStream)){
//                this.inputStream.setRawByteListeners(null);
                inputStream.close();
            }
        } catch (IOException ignore) {
            /*NOP*/
        } finally {
            setStatus(TaskStatus.STOP);
        }
    }



    public void setStatus(TaskStatus next) {
        connected.set(next);
//        doStatusListener(this, SyncerEvent.builder().taskId(configuration.getTaskId()).status(next).build());
    }


    protected boolean compareAndSet(TaskStatus prev, TaskStatus prev2,TaskStatus next) {
        boolean result = connected.compareAndSet(prev, next);
        if(!result){
            boolean result2=connected.compareAndSet(prev2,next);
            if(result2){
                return true;
            }else {
                return false;
            }
        }
        return true;
    }

    protected boolean compareAndSet(TaskStatus prev, TaskStatus next) {
        boolean result = connected.compareAndSet(prev, next);
//        if (result){
//            doTaskStatusListener(this, SyncerTaskEvent.builder().event(next).taskId(config.getTaskId()).replid(config.getReplId()).offset(config.getReplOffset()).build());
//        }
        return result;

    }

    public void submitEvent(Event event) {
        long offset = config.getReplOffset();
        submitEvent(event, Tuples.of(offset, offset),config.getReplId(),config.getReplOffset());
    }

    public void submitEvent(Event event, Tuple2<Long, Long> offsets,String replid,long currentCommandOffset) {
        try {
            dress(event, offsets,replid,currentCommandOffset);
            doEventListener(this, event);
        } catch (UncheckedIOException e) {
            throw e;
            //ignore UncheckedIOException so that to propagate to caller.
        } catch (Throwable e) {
//            doExceptionListener(this, e, event);
        }
    }

    public void submitEvent(Event event, Tuple2<Long, Long> offsets) {
        try {
            dress(event, offsets);
            doEventListener(this, event);
        } catch (UncheckedIOException e) {
            throw e;
            //ignore UncheckedIOException so that to propagate to caller.
        } catch (Throwable e) {
//            doExceptionListener(this, e, event);
        }
    }


    /**
     * 设置offset
     * @param event
     * @param offsets
     */
    protected void dress(Event event, Tuple2<Long, Long> offsets,String replid,long currentCommandOffset) {
        if (event instanceof AbstractEvent) {
            ((AbstractEvent) event).getContext().setOffset(offsets);
            ((AbstractEvent) event).getContext().setReplid(replid);
            ((AbstractEvent) event).getContext().setCurrentOffset(currentCommandOffset);
        }
    }

    protected void dress(Event event, Tuple2<Long, Long> offsets) {
        if (event instanceof AbstractEvent) {
            ((AbstractEvent) event).getContext().setOffset(offsets);
        }
    }

    public void submitSyncerTaskEvent(SyncerTaskEvent event) {
        try {
            doTaskStatusListener(this,event);
        } catch (Exception e) {
            throw e;
            //ignore UncheckedIOException so that to propagate to caller.
        }
    }


    /**
     * 注册命令解析器
     */
    @Override
    public void builtInCommandParserRegister() {

    }

    @Override
    public CommandParser<? extends Command> getCommandParser(CommandName command) {
        return commands.get(command);
    }

    @Override
    public <T extends Command> void addCommandParser(CommandName command, CommandParser<T> parser) {
        commands.put(command,parser);
    }

    @Override
    public CommandParser<? extends Command> removeCommandParser(CommandName command) {
        return commands.get(command);
    }

    @Override
    public ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion) {
        return modules.get(ModuleKey.key(moduleName, moduleVersion));
    }

    @Override
    public <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser) {
        modules.put(ModuleKey.key(moduleName, moduleVersion), parser);
    }

    @Override
    public ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion) {
        return modules.remove(ModuleKey.key(moduleName, moduleVersion));
    }

    @Override
    public void setRdbParser(IRdbParser rdbParser) {
        this.rdbParser=rdbParser;
    }

    @Override
    public IRdbParser getRdbVisitor() {
        return this.rdbParser;
    }

    @Override
    public TaskStatus getStatus() {
        return connected.get();
    }


    @Override
    public ReplicConfig getConfig() {
        return config;
    }


}
