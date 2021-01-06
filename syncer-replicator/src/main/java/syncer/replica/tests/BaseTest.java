package syncer.replica.tests;

import com.alibaba.fastjson.JSON;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.FileType;
import syncer.replica.event.*;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskStatusListener;
import syncer.replica.rdb.datatype.KeyStringValueString;
import syncer.replica.replication.AofReplication;
import syncer.replica.replication.RdbReplication;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.objectutil.Strings;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/21
 */
public class BaseTest {
    public static void main(String[] args) throws Exception {
        Configuration configuration=Configuration.defaultSetting().setTaskId("iiii").setReplOffset(0L);
        Replication replication=new RedisReplication("http://onlinefile.i1314i.com/appendonly.aof", FileType.ONLINEAOF, configuration);
//        Replication replication=new RedisReplication("redis://114.67.100.239:6379?authPassword=redistest0102");
        replication.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replication replicator, Event event) {

                if(event instanceof PreRdbSyncEvent){
//                    System.out.println("全量同步开始");
                }

                if(event instanceof PostRdbSyncEvent){
//                    System.out.println("全量同步结束");
                }

                if(event instanceof PreCommandSyncEvent){
//                    System.out.println("增量同步开始");
                }

                if (event instanceof KeyStringValueString) {
                    KeyStringValueString kv = (KeyStringValueString) event;
//                    System.out.println(new String(kv.getKey()));
//                    System.out.println(Strings.byteToString(kv.getValue()));
                } else {
//                    System.out.println(JSON.toJSONString(event));
                }
            }
        });

        replication.addTaskStatusListener(new TaskStatusListener() {
            @Override
            public void handle(Replication replication, SyncerTaskEvent event) {
                System.out.println(JSON.toJSONString(event));
            }
        });
        replication.open();
    }
}
