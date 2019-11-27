package syncerservice.syncerplusredis.extend.replicator.service;



import syncerservice.syncerplusredis.entity.Configuration;
import syncerservice.syncerplusredis.entity.FileType;
import syncerservice.syncerplusredis.entity.RedisURI;
import syncerservice.syncerplusredis.replicator.RedisReplicator;

import java.io.*;
import java.net.URISyntaxException;

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
