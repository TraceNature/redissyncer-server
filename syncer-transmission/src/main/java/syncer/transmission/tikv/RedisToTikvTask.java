package syncer.transmission.tikv;

import lombok.extern.slf4j.Slf4j;
import org.tikv.common.TiConfiguration;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;
import org.tikv.txn.TxnKVClient;
import syncer.common.util.TemplateUtils;
import syncer.replica.config.RedisURI;
import syncer.replica.datatype.command.common.PingCommand;
import syncer.replica.datatype.command.common.SelectCommand;
import syncer.replica.datatype.command.set.SetCommand;
import syncer.replica.event.AuxField;
import syncer.replica.event.Event;
import syncer.replica.event.KeyStringValueListEvent;
import syncer.replica.event.KeyStringValueStringEvent;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.register.TikvCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.transmission.tikv.processor.DefaultRedis2TikvProcessor;
import syncer.transmission.tikv.processor.IRedis2TikvProcessor;
import java.util.Objects;

/**
 *
 */
@Slf4j
public class RedisToTikvTask{
    private Replication replication = null;

    private String instId;
    private String redisHost;
    private Integer port;
    private String password;
    private String tikvUri;

    public RedisToTikvTask(String instId, String redisHost, Integer port, String password, String tikvUri) {
        this.instId = instId;
        this.redisHost = redisHost;
        this.port = port;
        this.password = password;
        this.tikvUri = tikvUri;
    }

    public void run() {
        try {
            String redisAddress=new StringBuilder(redisHost).append(":").append(port).toString();
            String sourceRedisUri=redisUri(redisAddress,password);
            RedisURI suri = new RedisURI(sourceRedisUri);
            replication = new RedisReplication(suri, true);
            replication.getConfig().setTaskId(TemplateUtils.uuid());
            RawKVClient kvClient=tikvRawClient(tikvUri);
            IRedis2TikvProcessor redis2TikvProcessor=new DefaultRedis2TikvProcessor();
            redis2TikvProcessor.load(replication.getConfig().getTaskId(),instId,tikvUri);
            /**
             * 注册解析器
             */
            TikvCommandRegister.addCommandParser(replication);
            try {
                replication.addEventListener(new EventListener() {
                    @Override
                    public void onEvent(Replication replication, Event event) {

                        if(event instanceof AuxField){
                            return;
                        }

                        //全量同步开始
                        if(event instanceof PreRdbSyncEvent){
                            redis2TikvProcessor.preRdbSyncEventHandler((PreRdbSyncEvent) event);
                            return;
                        }
                        //全量同步结束事件
                        if(event instanceof PostRdbSyncEvent){
                            redis2TikvProcessor.postRdbSyncEventHandler((PostRdbSyncEvent) event);
                            return;
                        }

                        //String rdb
                        if(event instanceof KeyStringValueStringEvent){
                            KeyStringValueStringEvent stringValueStringEvent= (KeyStringValueStringEvent) event;
                            redis2TikvProcessor.rdbStringHandler(stringValueStringEvent);
                            return;
                        }

                        //List todo
                        if(event instanceof KeyStringValueListEvent) {
                            KeyStringValueListEvent stringValueListEvent = (KeyStringValueListEvent) event;

                        }

                        //增量开始
                        if(event instanceof PreCommandSyncEvent){
                            redis2TikvProcessor.preCommandSyncEventHandler((PreCommandSyncEvent) event);
                            return;
                        }

                        //增量结束，用于aof导入
                        if(event instanceof PostCommandSyncEvent){
                            redis2TikvProcessor.postCommandSyncEventHandler((PostCommandSyncEvent) event);
                            return;
                        }

                        // SET key value [EX seconds|PX milliseconds|EXAT timestamp|PXAT milliseconds-timestamp|KEEPTTL] [NX|XX] [GET]
                        if(event instanceof SetCommand){
                            redis2TikvProcessor.setCommandHandler((SetCommand) event);
                            return;
                        }

                        if(event instanceof SelectCommand){
                            redis2TikvProcessor.selectCommandHandler((SelectCommand) event);
                            return;
                        }

                        if (event instanceof PingCommand){
                            redis2TikvProcessor.pingCommandHandler((PingCommand) event);
                            return;
                        }


                        log.info("no matching parser [{}]",event.getClass());
                    }

                    @Override
                    public String eventListenerName() {
                        return "redisToTikv_"+instId;
                    }
                });

            }finally {
                if(Objects.nonNull(kvClient)){
                    kvClient.close();
                }
            }
            replication.open();
        }catch (Exception e){
            e.printStackTrace();
        }


    }


     String redisUri(String address, String password) {
        StringBuilder uri = new StringBuilder("redis://");
        int index = 0;
        if (address != null && address.length() > 0) {
            uri.append(address);
            if (password != null && password.length() > 0) {
                index++;
                uri.append("?authPassword=");
                uri.append(password);
            }
        }
        return uri.toString();
    }

     RawKVClient tikvRawClient(String address){
        TiConfiguration conf = TiConfiguration.createRawDefault(address);
        TiSession session = TiSession.create(conf);
        RawKVClient client = session.createRawClient();
        return client;
    }

     TxnKVClient tikvTxnClient(String address){
        TiConfiguration conf = TiConfiguration.createRawDefault(address);
        TiSession session = TiSession.create(conf);
        TxnKVClient client = session.createTxnClient();
        return client;
    }
}
