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

import com.i1314i.syncerplusredis.cmd.*;
import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.event.PostCommandSyncEvent;
import com.i1314i.syncerplusredis.event.PreCommandSyncEvent;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.io.RedisInputStream;
import com.i1314i.syncerplusredis.util.objectutil.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;

import static com.i1314i.syncerplusredis.replicator.Status.CONNECTED;
import static com.i1314i.syncerplusredis.replicator.Status.DISCONNECTED;
import static com.i1314i.syncerplusredis.util.objectutil.Strings.format;
import static com.i1314i.syncerplusredis.util.type.Tuples.of;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
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
        if (configuration.isUseDefaultExceptionListener())
            addExceptionListener(new DefaultExceptionListener());
    }
    
    @Override
    public void open() throws IOException, IncrementException {
        super.open();
        if (!compareAndSet(DISCONNECTED, CONNECTED)) return;
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

    }

    protected void doOpen() throws IOException {
        configuration.setReplOffset(0L);
        submitEvent(new PreCommandSyncEvent());
        try {
            final long[] offset = new long[1];
            while (getStatus() == CONNECTED) {
                Object obj = replyParser.parse(len -> offset[0] = len);
                if (obj instanceof Object[]) {
                    if (verbose() && logger.isDebugEnabled())
                        logger.debug(format((Object[]) obj));
                    Object[] raw = (Object[]) obj;
                    CommandName name = CommandName.name(Strings.toString(raw[0]));
                    final CommandParser<? extends Command> parser;
                    if ((parser = commands.get(name)) == null) {
                        logger.warn("command [{}] not register. raw command:{}", name, format(raw));
                        configuration.addOffset(offset[0]);
                        offset[0] = 0L;
                        continue;
                    }
                    final long st = configuration.getReplOffset();
                    final long ed = st + offset[0];
                    submitEvent(parser.parse(raw), of(st, ed));
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
}