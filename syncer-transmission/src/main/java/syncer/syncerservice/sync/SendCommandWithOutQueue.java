package syncer.syncerservice.sync;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.filter.KeyValueRunFilterChain;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.common.Strings;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/9
 */

@Slf4j
@Builder
public class SendCommandWithOutQueue {
    private Replicator r;
    private KeyValueRunFilterChain filterChain;
    private String taskId;
    private boolean status = true;
    private ISyncerCompensator syncerCompensator;


    public SendCommandWithOutQueue(Replicator r, KeyValueRunFilterChain filterChain, String taskId, boolean status, ISyncerCompensator syncerCompensator) {
        this.r = r;
        this.filterChain = filterChain;
        this.taskId = taskId;
        this.status  = true;
        this.syncerCompensator = syncerCompensator;
    }

    void run(KeyValueEventEntity keyValueEventEntity){


            try {

                keyValueEventEntity.setISyncerCompensator(syncerCompensator);
                if(null!=keyValueEventEntity){
                    filterChain.run(r,keyValueEventEntity);
                }

            }catch (Exception e){
                Event event=keyValueEventEntity.getEvent();
                String keyName=null;
                String keyValue=null;
                if(event instanceof DefaultCommand){
                    DefaultCommand defaultCommand= (DefaultCommand) event;
                    if(defaultCommand.getArgs().length>0){
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand())+"  "+JSON.toJSONString(Strings.byteToString(((DefaultCommand) event).getArgs()));
                    }else{
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand());
                    }
                }else if(event instanceof DumpKeyValuePair){
                    DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
                    keyName= Strings.byteToString(dumpKeyValuePair.getKey());

                }else if(event instanceof BatchedKeyValuePair){
                    BatchedKeyValuePair batchedKeyValuePair= (BatchedKeyValuePair) event;
                    keyName=Strings.toString(batchedKeyValuePair.getKey());
                }

                log.warn("[{}]抛弃key:{} ,class:[{}]:原因[{}]",taskId, keyName,event.getClass().toString(),e.getMessage());
                log.warn("[{}]抛弃的命令byte:[{}]",taskId,JSON.toJSONString(event));
                log.warn("[{}]抛弃的命令格式:[{}]",taskId,keyName);
                e.printStackTrace();


            }


    }
}
