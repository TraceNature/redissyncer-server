package com.i1314i.syncerplusservice.replicator.service;

import com.moilioncircle.redis.replicator.*;
import com.moilioncircle.redis.replicator.io.PeekableInputStream;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Objects;

public class JDRedisReplicator  extends RedisReplicator implements Serializable {



    public JDRedisReplicator(File file, FileType fileType, Configuration configuration) throws FileNotFoundException {
        super(file, fileType, configuration);
    }

    public JDRedisReplicator(InputStream in, FileType fileType, Configuration configuration) {
        super(in, fileType, configuration);
    }

    public JDRedisReplicator(String host, int port, Configuration configuration) {
        super(host, port, configuration);
    }

    public JDRedisReplicator(String uri) throws URISyntaxException, IOException {
        super(uri);
    }

    public JDRedisReplicator(RedisURI uri) throws IOException {
        super(uri);
    }

    private void initialize(RedisURI uri) throws IOException {
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
            this.replicator = new JDRedisSocketReplicator(uri.getHost(), uri.getPort(), configuration);

        }
    }

    public  Long replayId(){
        if(this.replicator!=null){
            return this.replicator.getConfiguration().getReplOffset();
        }
        return -1L;
    }
}
