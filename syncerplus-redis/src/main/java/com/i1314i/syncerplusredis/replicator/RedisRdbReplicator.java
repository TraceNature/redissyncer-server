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

import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.exception.IncrementException;
import com.i1314i.syncerplusredis.io.RedisInputStream;
import com.i1314i.syncerplusredis.rdb.RdbParser;

import java.io.*;
import java.util.Objects;

import static com.i1314i.syncerplusredis.replicator.Status.CONNECTED;
import static com.i1314i.syncerplusredis.replicator.Status.DISCONNECTED;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class RedisRdbReplicator extends AbstractReplicator {
    
    public RedisRdbReplicator(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }
    
    public RedisRdbReplicator(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
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
        try {
            new RdbParser(inputStream, this).parse();
        } catch (EOFException ignore) {
        }
    }
}
