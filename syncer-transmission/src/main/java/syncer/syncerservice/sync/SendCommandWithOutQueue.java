package syncer.syncerservice.sync;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.filter.KeyValueRunFilterChain;
import syncer.syncerservice.po.KeyValueEventEntity;

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
//                System.out.println(JSON.toJSONString(queue.take()));
            try {


                if(null!=keyValueEventEntity){
                    filterChain.run(r,keyValueEventEntity);
                }

            }catch (Exception e){
                System.out.println(keyValueEventEntity.getEvent().getClass());
                log.warn("[{}]抛弃key:{}:原因[{}]",taskId, JSON.toJSONString(keyValueEventEntity.getEvent()),e.getMessage());
            }

        }catch (Exception e){
            try {
                log.warn("[{}]key从队列拿出失败:{}",taskId,e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
