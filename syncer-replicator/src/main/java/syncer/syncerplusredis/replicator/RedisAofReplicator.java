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

package syncer.syncerplusredis.replicator;

import syncer.syncerpluscommon.util.taskType.SyncerTaskType;
import syncer.syncerplusredis.cmd.jimdb.JimDbFirstCommandParser;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.event.PostCommandSyncEvent;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.io.RedisInputStream;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerplusredis.util.objectutil.Strings;
import syncer.syncerplusredis.util.type.Tuples;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syncer.syncerplusredis.cmd.*;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static syncer.syncerplusredis.util.type.Tuples.of;

/**
 * @author Leon Chen
 * @since 2.1.0
 */

@Slf4j
public class RedisAofReplicator extends AbstractReplicator {

    protected static final Logger logger = LoggerFactory.getLogger(RedisAofReplicator.class);
    protected final ReplyParser replyParser;

    public RedisAofReplicator(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RedisAofReplicator(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener()) {
            addExceptionListener(new DefaultExceptionListener());
        }
    }


    public RedisAofReplicator(String filePath, Configuration configuration, String taskId) {
        InputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            try {
                if(!SyncerTaskType.isMultiTask(taskId)){
                    TaskDataManagerUtils.updateThreadStatusAndMsg(taskId,"文件读取异常", TaskStatusType.BROKEN);
                }else {
                    MultiSyncTaskManagerutils.setGlobalNodeStatus(taskId,"文件读取异常", TaskStatusType.BROKEN);

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, "文件下载异常");
        }

        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener()) {
            addExceptionListener(new DefaultExceptionListener());
        }

    }

    @Override
    public void open() throws IOException, IncrementException {
        super.open();
        if (!compareAndSet(Status.DISCONNECTED, Status.CONNECTED)) {
            return;
        }
        try {
            doOpen();
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)){
                throw e.getCause();
            }
        } finally {
            doClose();
            doCloseListener(this);
        }
    }

    @Override
    public void open(String taskId) throws IOException, IncrementException {
        super.open();
        if (!compareAndSet(Status.DISCONNECTED, Status.CONNECTED)) {
            return;
        }
        try {
            doOpen(taskId);
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)){
                throw e.getCause();
            }
        } finally {
            doClose();
            doCloseListener(this);
        }
    }

    protected void doOpen() throws IOException {
        configuration.setReplOffset(0L);
        submitEvent(new PreCommandSyncEvent());
        try {
            final long[] offset = new long[1];
            while (getStatus() == Status.CONNECTED) {
                Object obj = replyParser.parse(len -> offset[0] = len);
                if (obj instanceof Object[]) {
                    if (verbose() && logger.isDebugEnabled()){
                        logger.debug(Strings.format((Object[]) obj));
                    }

                    Object[] raw = (Object[]) obj;
                    CommandName name = CommandName.name(Strings.toString(raw[0]));
                    final CommandParser<? extends Command> parser;
                    if ((parser = commands.get(name)) == null) {
                        logger.warn("command [{}] not register. raw command:{}", name, Strings.format(raw));
                        configuration.addOffset(offset[0]);
                        offset[0] = 0L;
                        continue;
                    }
                    final long st = configuration.getReplOffset();
                    final long ed = st + offset[0];
                    submitEvent(parser.parse(raw), Tuples.of(st, ed));
                } else {
                    logger.warn("unexpected redis reply:{}", obj);
                }
                configuration.addOffset(offset[0]);
                offset[0] = 0L;
            }
        } catch (EOFException ignore) {
            submitEvent(new PostCommandSyncEvent());
        }
    }


    protected void doOpen(String taskId) throws IOException {
        try {
            configuration.setReplOffset(0L);
            submitEvent(new PreCommandSyncEvent());
            try {
                final long[] offset = new long[1];
                while (getStatus() == Status.CONNECTED) {
                    Object obj = replyParser.parse(len -> offset[0] = len);
                    if (obj instanceof Object[]) {
                        if (verbose() && logger.isDebugEnabled()) {
                            logger.debug(Strings.format((Object[]) obj));
                        }
                        Object[] raw = (Object[]) obj;

                        CommandName name = CommandName.name(Strings.toString(raw[0]));

//                        //jimdb 首次解析
//                        if(name.equals("TRANSMIT")){
//                            CommandName first_parser_name = CommandName.name("TRANSMITFIRSTPARSER");
//                            final JimDbFirstCommandParser firstparser= (JimDbFirstCommandParser) commands.get(first_parser_name);
//                            if(firstparser!=null){
//                                raw=firstparser.parse(raw).getCommand();
//                            }
//                        }

                        final CommandParser<? extends Command> parser;
                        if ((parser = commands.get(name)) == null) {
                            logger.warn("command [{}] not register. raw command:{}", name, Strings.format(raw));
                            configuration.addOffset(offset[0]);
                            offset[0] = 0L;
                            continue;
                        }

                        final long st = configuration.getReplOffset();
                        final long ed = st + offset[0];
                        submitEvent(parser.parse(raw), Tuples.of(st, ed));
                    } else {
                        logger.warn("unexpected redis reply:{}", obj);
                    }
                    configuration.addOffset(offset[0]);
                    offset[0] = 0L;
                }
            } catch (EOFException ignore) {
                submitEvent(new PostCommandSyncEvent());
            }
        } catch (EOFException ignore) {
            try {
                if(!SyncerTaskType.isMultiTask(taskId)){
                    TaskDataManagerUtils.updateThreadStatusAndMsg(taskId,ignore.getMessage(), TaskStatusType.BROKEN);
                }else {
                    MultiSyncTaskManagerutils.setGlobalNodeStatus(taskId,"文件读取异常", TaskStatusType.BROKEN);

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ignore.getMessage());
        }
    }
}