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

package syncerservice.syncerplusredis.replicator;

import syncerservice.syncerplusredis.entity.Configuration;
import syncerservice.syncerplusredis.event.PostCommandSyncEvent;
import syncerservice.syncerplusredis.event.PreCommandSyncEvent;
import syncerservice.syncerplusredis.exception.IncrementException;
import syncerservice.syncerplusredis.exception.TaskMsgException;
import syncerservice.syncerplusredis.io.PeekableInputStream;
import syncerservice.syncerplusredis.io.RedisInputStream;
import syncerservice.syncerplusredis.rdb.RdbParser;
import syncerservice.syncerplusredis.util.TaskMsgUtils;
import syncerservice.syncerplusredis.util.objectutil.Strings;
import syncerservice.syncerplusredis.util.type.Tuples;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syncerservice.syncerplusredis.cmd.*;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static syncerservice.syncerplusredis.util.type.Tuples.of;

/**
 * @author Leon Chen
 * @since 2.1.0
 */

@Slf4j
public class RedisMixReplicator extends AbstractReplicator {
    protected static final Logger logger = LoggerFactory.getLogger(RedisMixReplicator.class);
    protected final ReplyParser replyParser;
    protected final PeekableInputStream peekable;

    public RedisMixReplicator(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RedisMixReplicator(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        if (in instanceof PeekableInputStream) {
            this.peekable = (PeekableInputStream) in;
        } else {
            in = this.peekable = new PeekableInputStream(in);
        }
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener())
            addExceptionListener(new DefaultExceptionListener());
    }


    public RedisMixReplicator(String filePath, Configuration configuration, String taskId) {
        InputStream in = null;
        try {
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId),"文件读取异常");
            } catch (TaskMsgException ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, "文件下载异常");
        }

        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        if (in instanceof PeekableInputStream) {
            this.peekable = (PeekableInputStream) in;
        } else {
            in = this.peekable = new PeekableInputStream(in);
        }
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener())
            addExceptionListener(new DefaultExceptionListener());
    }

    @Override
    public void open() throws IOException, IncrementException {
        super.open();
        if (!compareAndSet(Status.DISCONNECTED, Status.CONNECTED)) return;
        try {
            doOpen();
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)) throw e.getCause();
        } finally {
            doClose();
            doCloseListener(this);
        }
    }

    @Override
    public void open(String taskId) throws IOException, IncrementException {
        super.open();
        if (!compareAndSet(Status.DISCONNECTED, Status.CONNECTED)) return;
        try {
            doOpen(taskId);
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)) throw e.getCause();
        } finally {
            doClose();
            doCloseListener(this);
        }
    }

    protected void doOpen() throws IOException {
        configuration.setReplOffset(0L);
        if (peekable.peek() == 'R') {
            RdbParser parser = new RdbParser(inputStream, this);
            configuration.setReplOffset(parser.parse());
        }
        if (getStatus() != Status.CONNECTED) return;
        submitEvent(new PreCommandSyncEvent());
        try {
            final long[] offset = new long[1];
            while (getStatus() == Status.CONNECTED) {
                Object obj = replyParser.parse(len -> offset[0] = len);
                if (obj instanceof Object[]) {
                    if (verbose() && logger.isDebugEnabled())
                        logger.debug(Strings.format((Object[]) obj));
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
            if (peekable.peek() == 'R') {
                RdbParser parser = new RdbParser(inputStream, this);
                configuration.setReplOffset(parser.parse());
            }
            if (getStatus() != Status.CONNECTED) return;
            submitEvent(new PreCommandSyncEvent());
            try {
                final long[] offset = new long[1];
                while (getStatus() == Status.CONNECTED) {
                    Object obj = replyParser.parse(len -> offset[0] = len);
                    if (obj instanceof Object[]) {
                        if (verbose() && logger.isDebugEnabled())
                            logger.debug(Strings.format((Object[]) obj));
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

        } catch (Exception e) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId),e.getMessage());
            } catch (TaskMsgException ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }
    }
}
