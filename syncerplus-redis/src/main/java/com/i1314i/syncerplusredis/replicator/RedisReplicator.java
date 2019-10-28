/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i1314i.syncerplusredis.replicator;

import com.i1314i.syncerplusredis.RedisSocketReplicator;
import com.i1314i.syncerplusredis.cmd.Command;
import com.i1314i.syncerplusredis.cmd.CommandName;
import com.i1314i.syncerplusredis.cmd.CommandParser;
import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.entity.FileType;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.event.EventListener;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.io.PeekableInputStream;
import com.i1314i.syncerplusredis.io.RawByteListener;
import com.i1314i.syncerplusredis.rdb.RdbVisitor;
import com.i1314i.syncerplusredis.rdb.datatype.Module;
import com.i1314i.syncerplusredis.rdb.module.ModuleParser;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class RedisReplicator implements Replicator {
    protected Replicator replicator;

    public RedisReplicator(File file, FileType fileType, Configuration configuration) throws FileNotFoundException {
        switch (fileType) {
            case AOF:
                this.replicator = new RedisAofReplicator(file, configuration);
                break;
            case RDB:
                this.replicator = new RedisRdbReplicator(file, configuration);
                break;
            case MIXED:
                this.replicator = new RedisMixReplicator(file, configuration);
                break;
            default:
                throw new UnsupportedOperationException(fileType.toString());
        }
    }

    public RedisReplicator(InputStream in, FileType fileType, Configuration configuration) {
        switch (fileType) {
            case AOF:
                this.replicator = new RedisAofReplicator(in, configuration);
                break;
            case RDB:
                this.replicator = new RedisRdbReplicator(in, configuration);
                break;
            case MIXED:
                this.replicator = new RedisMixReplicator(in, configuration);
                break;
            case ONLINERDB:
                this.replicator = new RedisOnlineRdbReplicator(in, configuration);
            default:
                throw new UnsupportedOperationException(fileType.toString());
        }
    }

    public RedisReplicator(InputStream in, FileType fileType, String fileUrl, Configuration configuration) {
        try {
            switch (fileType) {
                case AOF:
                    this.replicator = new RedisAofReplicator(in, configuration);
                    break;
                case RDB:
                    this.replicator = new RedisRdbReplicator(in, configuration);
                    break;
                case MIXED:
                    this.replicator = new RedisMixReplicator(in, configuration);
                    break;
                case ONLINERDB:
                    this.replicator = new RedisOnlineRdbReplicator(fileUrl, configuration);
                    break;
                case ONLINEAOF:
                    this.replicator = new RedisOnlineAofReplicator(fileUrl, configuration);
                    break;
                default:
                    throw new UnsupportedOperationException(fileType.toString());
            }

        } catch (Exception e) {

        }
    }

    public RedisReplicator(String host, int port, Configuration configuration, boolean status) {
        loadingRedisReplicator(host, port, configuration, status);
    }


    private void loadingRedisReplicator(String host, int port, Configuration configuration, boolean status) {
        this.replicator = new RedisSocketReplicator(host, port, configuration, status);
    }

    public RedisReplicator(String host, int port, Configuration configuration) {
        loadingRedisReplicator(host, port, configuration, true);
    }


    /**
     * @param uri redis uri.
     * @throws URISyntaxException uri syntax error.
     * @throws IOException        read timeout or read EOF.
     * @see RedisURI
     * @since 2.4.0
     */
    public RedisReplicator(String uri) throws URISyntaxException, IOException {
        Objects.requireNonNull(uri);
        initialize(new RedisURI(uri), true);
    }

    /**
     * @param uri redis uri.
     * @throws IOException read timeout or read EOF.
     * @since 2.4.2
     */
    public RedisReplicator(RedisURI uri) throws IOException {
        loadRedisReplicator(uri, true);
    }

    private void loadRedisReplicator(RedisURI uri, boolean status) throws IOException {
        initialize(uri, status);
    }

    public RedisReplicator(RedisURI uri, boolean status) throws IOException {
//        loadRedisReplicator(uri,status);
        initialize(uri, status);
    }

    private void initialize(RedisURI uri, boolean status) throws IOException {
        Objects.requireNonNull(uri);
        Configuration configuration = Configuration.valueOf(uri);
        if (uri.getFileType() != null) {
            PeekableInputStream in = new PeekableInputStream(uri.toURL().openStream());
            switch (uri.getFileType()) {
                case AOF:
                    if (in.peek() == 'R') {
                        this.replicator = new RedisMixReplicator(in, configuration);
                    } else {
                        this.replicator = new RedisAofReplicator(in, configuration);
                    }
                    break;
                case RDB:
                    this.replicator = new RedisRdbReplicator(in, configuration);
                    break;
                case MIXED:
                    this.replicator = new RedisMixReplicator(in, configuration);
                    break;
                default:
                    throw new UnsupportedOperationException(uri.getFileType().toString());
            }
        } else {

            this.replicator = new RedisSocketReplicator(uri.getHost(), uri.getPort(), configuration, status);

//            if(status){
//                this.replicator = new RedisSocketReplicator(uri.getHost(), uri.getPort(), configuration,status);
//            }else {
//                System.out.println("==="+status);
//                this.replicator = new JDAofRedisSocketReplicator(uri.getHost(), uri.getPort(), configuration);
//            }

//            this.replicator = new RedisSocketReplicator(uri.getHost(), uri.getPort(), configuration);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Replicator> T getReplicator() {
        return (T) this.replicator;
    }

    @Override
    public boolean addRawByteListener(RawByteListener listener) {
        return replicator.addRawByteListener(listener);
    }

    @Override
    public boolean removeRawByteListener(RawByteListener listener) {
        return replicator.removeRawByteListener(listener);
    }

    @Override
    public void builtInCommandParserRegister() {
        replicator.builtInCommandParserRegister();
    }

    @Override
    public CommandParser<? extends Command> getCommandParser(CommandName command) {
        return replicator.getCommandParser(command);
    }

    @Override
    public <T extends Command> void addCommandParser(CommandName command, CommandParser<T> parser) {
        replicator.addCommandParser(command, parser);
    }

    @Override
    public CommandParser<? extends Command> removeCommandParser(CommandName command) {
        return replicator.removeCommandParser(command);
    }

    @Override
    public ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion) {
        return replicator.getModuleParser(moduleName, moduleVersion);
    }

    @Override
    public <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser) {
        replicator.addModuleParser(moduleName, moduleVersion, parser);
    }

    @Override
    public ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion) {
        return replicator.removeModuleParser(moduleName, moduleVersion);
    }

    @Override
    public void setRdbVisitor(RdbVisitor rdbVisitor) {
        replicator.setRdbVisitor(rdbVisitor);
    }

    @Override
    public RdbVisitor getRdbVisitor() {
        return replicator.getRdbVisitor();
    }

    @Override
    public boolean addEventListener(EventListener listener) {
        return replicator.addEventListener(listener);
    }

    @Override
    public boolean removeEventListener(EventListener listener) {
        return replicator.removeEventListener(listener);
    }

    @Override
    public boolean addCloseListener(CloseListener listener) {
        return replicator.addCloseListener(listener);
    }

    @Override
    public boolean removeCloseListener(CloseListener listener) {
        return replicator.removeCloseListener(listener);
    }

    @Override
    public boolean addExceptionListener(ExceptionListener listener) {
        return replicator.addExceptionListener(listener);
    }

    @Override
    public boolean removeExceptionListener(ExceptionListener listener) {
        return replicator.removeExceptionListener(listener);
    }

    @Override
    public boolean addStatusListener(StatusListener listener) {
        return replicator.addStatusListener(listener);
    }

    @Override
    public boolean removeStatusListener(StatusListener listener) {
        return replicator.removeStatusListener(listener);
    }

    @Override
    public boolean verbose() {
        return replicator.verbose();
    }

    @Override
    public Status getStatus() {
        return replicator.getStatus();
    }

    @Override
    public Configuration getConfiguration() {
        return replicator.getConfiguration();
    }

    @Override
    public void open() throws IOException, IncrementException {
        replicator.open();
    }

    @Override
    public void open(String taskId) throws IOException, IncrementException {
        replicator.open(taskId);
    }

    @Override
    public void close() throws IOException {
        replicator.close();
    }
}
