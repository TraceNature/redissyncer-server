package com.i1314i.syncerplusredis.extend.replicator.service;



import com.i1314i.syncerplusredis.entity.Configuration;
import com.i1314i.syncerplusredis.entity.FileType;
import com.i1314i.syncerplusredis.entity.RedisURI;
import com.i1314i.syncerplusredis.io.PeekableInputStream;
import com.i1314i.syncerplusredis.replicator.RedisAofReplicator;
import com.i1314i.syncerplusredis.replicator.RedisMixReplicator;
import com.i1314i.syncerplusredis.replicator.RedisRdbReplicator;
import com.i1314i.syncerplusredis.replicator.RedisReplicator;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Objects;

public class JDRedisReplicator  extends RedisReplicator implements Serializable {



    public JDRedisReplicator(File file, FileType fileType, Configuration configuration) throws FileNotFoundException {
        super(file, fileType, configuration);
    }

    public JDRedisReplicator(InputStream in, FileType fileType, String fileUrl, Configuration configuration,String taskId) {
        super(in, fileType, fileUrl, configuration,taskId);
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

    public JDRedisReplicator(RedisURI uri,boolean status) throws IOException {
        super(uri,status);
    }



    public  Long replayId(){
        if(this.replicator!=null){
            return this.replicator.getConfiguration().getReplOffset();
        }
        return -1L;
    }
}
