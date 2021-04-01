import com.alibaba.fastjson.JSON;
import org.junit.Test;
import syncer.replica.config.ReplicConfig;
import syncer.replica.datatype.command.DefaultCommand;
import syncer.replica.event.Event;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.parser.DefaultRedisRdbParser;
import syncer.replica.parser.iterable.IterableEventListener;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.replication.SocketReplication;
import syncer.replica.replication.Replication;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.TaskStatusListener;

import java.io.IOException;

public class ReplicaTest {

    @Test
    public void socketReplicaTest(){
        SocketReplication replication=new SocketReplication("127.0.0.1",6379, ReplicConfig.defaultConfig(),true);
        DefaultCommandRegister.addCommandParser(replication);
        replication.setRdbParser(new DefaultRedisRdbParser(replication));
        replication.addTaskStatusListener(new TaskStatusListener() {
            @Override
            public void handler(Replication replication, SyncerTaskEvent event) {
                System.out.println(JSON.toJSONString(event));
            }

            @Override
            public String eventListenerName() {
                return null;
            }
        });
        replication.addEventListener(new IterableEventListener(new EventListener() {
            @Override
            public void onEvent(Replication replication, Event event) {
                System.out.println(event.getClass());
                if(event instanceof DefaultCommand){
                    System.out.println(JSON.toJSONString(event));
                }
//                System.out.println(JSON.toJSONString(event));
//                if(event instanceof PreCommandSyncEvent){
//                    try {
//                        System.out.println("-----");
//                        replication.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                event=null;
            }

            @Override
            public String eventListenerName() {
                return "listener";
            }
        }));

        try {
            replication.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
