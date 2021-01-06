package syncer.replica.replication;

import syncer.replica.cmd.Command;
import syncer.replica.cmd.CommandName;
import syncer.replica.cmd.CommandParser;
import syncer.replica.entity.*;
import syncer.replica.io.PeekableInputStream;
import syncer.replica.listener.*;
import syncer.replica.rdb.RedisRdbVisitor;
import syncer.replica.rdb.datatype.Module;
import syncer.replica.rdb.module.ModuleParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
public class RedisReplication implements Replication {
    protected Replication replication;
    public RedisReplication(String host, int port, Configuration configuration) {
        this.replication = new SocketReplication(host, port, configuration,true);
    }

    /**
     * 本地文件
     * @param filePath
     * @param fileType
     * @param configuration
     * @throws Exception
     */
    public RedisReplication(String filePath, FileType fileType, Configuration configuration) throws Exception{
        switch (fileType) {
            case AOF:
                this.replication = new AofReplication(filePath, configuration,false);
                break;
            case RDB:
                this.replication = new RdbReplication(filePath, configuration,false);
                break;
            case MIXED:
                this.replication = new MixReplication(filePath, configuration,false);
                break;
            case ONLINEAOF:
                this.replication = new AofReplication(filePath, configuration,true);
                break;
            case ONLINERDB:
                this.replication = new RdbReplication(filePath, configuration,true);
                break;
            case ONLINEMIXED:
                this.replication = new MixReplication(filePath, configuration,true);
                break;
            default:
                throw new UnsupportedOperationException(fileType.toString());
        }
    }



    public  String replayId(){
        if(this.replication!=null){
            return this.replication.getConfiguration().getReplId();
        }
        return null;
    }
    public  Long offset(){
        if(this.replication!=null){
            return this.replication.getConfiguration().getReplOffset();
        }
        return -1L;
    }

    /**
     * @param uri redis uri.
     * @throws URISyntaxException uri syntax error.
     * @throws IOException        read timeout or read EOF.
     * @see RedisURI
     * @since 2.4.0
     */
    public RedisReplication(String uri) throws URISyntaxException, IOException {
        Objects.requireNonNull(uri);
        initialize(new RedisURI(uri), null,true);
    }

    /**
     * @param uri redis uri.
     * @throws IOException read timeout or read EOF.
     * @since 2.4.2
     */
    public RedisReplication(RedisURI uri) throws IOException {
        initialize(uri, null,true);
    }

    /**
     * @param uri redis uri.
     * @param sslConfiguration ssl configuration.
     * @throws URISyntaxException uri syntax error.
     * @throws IOException        read timeout or read EOF.
     * @see RedisURI
     * @see SslConfiguration
     * @since 3.4.0
     */
    public RedisReplication(String uri, SslConfiguration sslConfiguration) throws URISyntaxException, IOException {
        Objects.requireNonNull(uri);
        initialize(new RedisURI(uri), sslConfiguration,true);
    }

    /**
     * @param uri redis uri.
     * @param sslConfiguration ssl configuration.
     * @throws IOException read timeout or read EOF.
     * @see RedisURI
     * @see SslConfiguration
     * @since 3.4.0
     */
    public RedisReplication(RedisURI uri, SslConfiguration sslConfiguration) throws IOException {
        initialize(uri, sslConfiguration,true);
    }

    /**
     * 传入status来决定遇到 full resync的时候的处理方式
     * @param uri
     * @param status
     * @throws IOException
     */
    public RedisReplication(RedisURI uri, boolean status) throws IOException {
        initialize(uri, null,status);
    }

//    public RedisReplication(RedisURI uri, boolean status) throws IOException {
//        initialize(uri, null,status);
//    }

    /**
     * 传入status来决定遇到 full resync的时候的处理方式
     * @param uri
     * @param sslConfiguration
     * @param status
     * @throws IOException
     */
    public RedisReplication(RedisURI uri, SslConfiguration sslConfiguration, boolean status) throws IOException {
        initialize(uri, sslConfiguration,status);
    }

    private void initialize(RedisURI uri, SslConfiguration sslConfiguration, boolean status) throws IOException {
        Objects.requireNonNull(uri);
        Configuration configuration = Configuration.valueOf(uri).merge(sslConfiguration);
        if (uri.getFileType() != null) {

            PeekableInputStream in = new PeekableInputStream(uri.toURL().openStream());


            switch (uri.getFileType()) {
                case AOF:
                    if (in.peek() == 'R') {
                        this.replication = new MixReplication(in, configuration);
                    } else {
                        this.replication = new AofReplication(in, configuration);
                    }
                    break;
                case RDB:
                    this.replication = new RdbReplication(in, configuration);
                    break;
                case MIXED:
                    this.replication = new MixReplication(in, configuration);
                    break;
                case ONLINEAOF:
                    if (in.peek() == 'R') {
                        this.replication = new MixReplication(in, configuration);
                    } else {
                        this.replication = new AofReplication(in, configuration);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(uri.getFileType().toString());
            }
        } else {
            this.replication = new SocketReplication(uri.getHost(), uri.getPort(), configuration,status);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Replication> T getReplicator() {
        return (T) this.replication;
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
    public boolean addEventListener(EventListener listener) {
        return replication.addEventListener(listener);
    }

    @Override
    public boolean removeEventListener(EventListener listener) {
        return replication.removeEventListener(listener);
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
        return replication.removeTaskStatusListener(listener);
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
    public void open() throws IOException {
        replication.open();
    }

    @Override
    public void close() throws IOException {
        replication.close();
    }

}

