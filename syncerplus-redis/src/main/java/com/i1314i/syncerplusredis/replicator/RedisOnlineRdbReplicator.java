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
import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.exception.TaskMsgException;
import com.i1314i.syncerplusredis.io.RedisInputStream;
import com.i1314i.syncerplusredis.rdb.RdbParser;
import com.i1314i.syncerplusredis.util.TaskMsgUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

import static com.i1314i.syncerplusredis.replicator.Status.CONNECTED;
import static com.i1314i.syncerplusredis.replicator.Status.DISCONNECTED;
import static com.i1314i.syncerplusredis.util.objectutil.Concurrents.terminateQuietly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @since 4.0
 */
@Slf4j
public class RedisOnlineRdbReplicator extends AbstractReplicator {

//    public RedisOnlineRdbReplicator(String  fileUrl, Configuration configuration) {
//        this(fileUrl, configuration);
//    }

    public RedisOnlineRdbReplicator(InputStream in, Configuration configuration) {

        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        if (configuration.isUseDefaultExceptionListener())
            addExceptionListener(new DefaultExceptionListener());
    }


    public RedisOnlineRdbReplicator(String fileUrl, Configuration configuration,String taskId) {
        try {

            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
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
            this.inputStream.setRawByteListeners(this.rawByteListeners);
            if (configuration.isUseDefaultExceptionListener())
                addExceptionListener(new DefaultExceptionListener());

        }catch (Exception e){
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId,"文件下载异常");
        }
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

        super.open();
        if (!compareAndSet(DISCONNECTED, CONNECTED)) return;
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
        try {
            new RdbParser(inputStream, this).parse();
        } catch (EOFException ignore) {

        }
    }

    protected void doOpen(String taskId) throws IOException {
        try {
            new RdbParser(inputStream, this).parse();
        } catch (EOFException ignore) {
            try {
                Map<String, String> msg = TaskMsgUtils.brokenCreateThread(Arrays.asList(taskId));
            } catch (TaskMsgException ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId,ignore.getMessage());
        }
    }
}
