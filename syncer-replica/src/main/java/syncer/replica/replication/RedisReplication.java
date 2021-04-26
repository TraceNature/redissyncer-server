package syncer.replica.replication;

import syncer.jedis.HostAndPort;
import syncer.replica.config.RedisURI;
import syncer.replica.config.ReplicConfig;
import syncer.replica.config.SslReplicConfig;
import syncer.replica.constant.RedisType;
import syncer.replica.datatype.command.Command;
import syncer.replica.datatype.command.CommandName;
import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.io.PeekableInputStream;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskRawByteListener;
import syncer.replica.listener.TaskStatusListener;
import syncer.replica.parser.IRdbParser;
import syncer.replica.status.TaskStatus;
import syncer.replica.type.FileType;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class RedisReplication implements Replication{
    protected Replication replication;
    public RedisReplication(String host, int port, ReplicConfig config,RedisType redisType) {
        if(RedisType.SENTINEL.equals(redisType)){
            this.replication=new SentinelReplication(null,config.getMasterRedisName(),config,true);
        }else  {
            this.replication = new SocketReplication(host, port, config,true);
        }

    }


    public RedisReplication(String host, int port, ReplicConfig config){
        this(host,port,config,RedisType.SINGLE);
    }

    /**
     * @param uri redis uri.
     * @throws IOException read timeout or read EOF.
     * @since 2.4.2
     */
    public RedisReplication(RedisURI uri) throws IOException {
        initialize(uri, null,true,RedisType.SINGLE,null);
    }

    /**
     * 传入status来决定遇到 full resync的时候的处理方式
     * @param uri
     * @param status
     * @throws IOException
     */
    public RedisReplication(RedisURI uri, boolean status,List<HostAndPort> hosts) throws IOException {
        initialize(uri, null,status,RedisType.SINGLE,hosts);
    }

    public RedisReplication(RedisURI uri, boolean status) throws IOException {
        initialize(uri, null,status,RedisType.SINGLE,null);
    }

    public RedisReplication(RedisURI uri, boolean status,RedisType redisType,List<HostAndPort> hosts) throws IOException {
        initialize(uri, null,status,redisType,hosts);
    }


    /**
     * 本地文件
     * @param filePath
     * @param fileType
     * @param config
     * @throws Exception
     */
    public RedisReplication(String filePath, FileType fileType, ReplicConfig config) throws Exception{

        switch (fileType) {
            case AOF:
                this.replication = new AofReplication(filePath, config,false);
                break;
            case RDB:
                this.replication = new RdbReplication(filePath, config,false);
                break;
            case MIXED:
                this.replication = new MixedReplication(filePath, config,false);
                break;
            case ONLINEAOF:
                this.replication = new AofReplication(filePath, config,true);
                break;
            case ONLINERDB:
                this.replication = new RdbReplication(filePath, config,true);
                break;
            case ONLINEMIXED:
                this.replication = new MixedReplication(filePath, config,true);
                break;
            default:
                throw new UnsupportedOperationException(fileType.toString());
        }
    }



    /**
     * 传入status来决定遇到 full resync的时候的处理方式
     * @param uri
     * @param sslConfig
     * @param status
     * @throws IOException
     */
    public RedisReplication(RedisURI uri, SslReplicConfig sslConfig, boolean status) throws IOException {
        initialize(uri, sslConfig,status,RedisType.SINGLE,null);
    }

    private void initialize(RedisURI uri, SslReplicConfig sslConfig, boolean status, RedisType redisType, List<HostAndPort> hosts) throws IOException {
        Objects.requireNonNull(uri);
        ReplicConfig configuration = ReplicConfig.valueOf(uri).merge(sslConfig);
        if (uri.getFileType() != null) {

            PeekableInputStream in = new PeekableInputStream(uri.toURL().openStream());

            switch (uri.getFileType()) {
                case AOF:
                    if (in.peek() == 'R') {
                        this.replication = new MixedReplication(in, configuration);
                    } else {
                        this.replication = new AofReplication(in, configuration);
                    }
                    break;
                case RDB:
                    this.replication = new RdbReplication(in, configuration);
                    break;
                case MIXED:
                    this.replication = new MixedReplication(in, configuration);
                    break;
                case ONLINEAOF:
                    if (in.peek() == 'R') {
                        this.replication = new MixedReplication(in, configuration);
                    } else {
                        this.replication = new AofReplication(in, configuration);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(uri.getFileType().toString());
            }
        } else {
            if(RedisType.SENTINEL.equals(redisType)){
                System.out.println(configuration.getMasterRedisName()+":"+configuration.getSentinelAuthPassword());
                this.replication=new SentinelReplication(hosts,configuration.getMasterRedisName(),configuration,status);
            }else {
                this.replication = new SocketReplication(uri.getHost(), uri.getPort(), configuration,status);
            }

        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Replication> T getReplicator() {
        return (T) this.replication;
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
        replication.addCommandParser(command,parser);
    }

    @Override
    public CommandParser<? extends Command> removeCommandParser(CommandName command) {
        return replication.removeCommandParser(command);
    }

    @Override
    public ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion) {
        return replication.getModuleParser(moduleName,moduleVersion);
    }

    @Override
    public <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser) {
        replication.addModuleParser(moduleName,moduleVersion,parser);
    }

    @Override
    public ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion) {
        return replication.removeModuleParser(moduleName,moduleVersion);
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
        replication.open();
    }

    @Override
    public void close() throws IOException {
        replication.close();
    }

    @Override
    public void broken(String reason) throws IOException {
        replication.broken(reason);
    }
}