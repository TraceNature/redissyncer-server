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
import syncerservice.syncerplusredis.exception.TaskMsgException;
import syncerservice.syncerplusredis.io.RedisInputStream;
import syncerservice.syncerplusredis.util.TaskMsgUtils;
import syncerservice.syncerplusredis.util.objectutil.Strings;
import syncerservice.syncerplusredis.util.type.Tuples;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syncerservice.syncerplusredis.exception.IncrementException;
import syncerservice.syncerplusredis.cmd.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static syncerservice.syncerplusredis.util.type.Tuples.of;

/**
 * @since 4.0
 */

@Slf4j
public class RedisOnlineAofReplicator extends AbstractReplicator {

    protected static final Logger logger = LoggerFactory.getLogger(RedisOnlineAofReplicator.class);
    protected final ReplyParser replyParser;

    public RedisOnlineAofReplicator(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RedisOnlineAofReplicator(InputStream in, Configuration configuration) {
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

    public RedisOnlineAofReplicator(String fileUrl, Configuration configuration,String taskId) {
        try{
            URL url = new URL(fileUrl);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection)url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3*1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            InputStream in = conn.getInputStream();

            Objects.requireNonNull(in);
            Objects.requireNonNull(configuration);
            this.configuration = configuration;
            this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());

        }catch (IOException e){
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId),"文件在线读取异常");
            } catch (TaskMsgException ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId,"文件下载异常");
        
        }

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
            doOpen();
        } catch (UncheckedIOException e) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId),e.getMessage());
            } catch (TaskMsgException ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId,e.getMessage());
            if (!(e.getCause() instanceof EOFException)) throw e.getCause();
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
}