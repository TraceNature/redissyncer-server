package syncer.transmission.task;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.common.util.ThreadPoolUtils;
import syncer.replica.replication.Replication;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.po.entity.KeyValueEventEntity;
import syncer.transmission.queue.SyncerQueue;
import syncer.transmission.strategy.commandprocessing.ProcessingRunStrategyChain;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;


import java.io.IOException;

@Slf4j
@Builder
public class SendCommandTask implements Runnable{
    private Replication replication;
    private ProcessingRunStrategyChain filterChain;
    private SyncerQueue<KeyValueEventEntity> queue;
    private String taskId;
    private boolean status = true;
    private ISyncerCompensator syncerCompensator;

    public SendCommandTask(Replication replication, ProcessingRunStrategyChain filterChain, SyncerQueue<KeyValueEventEntity> queue, String taskId, boolean status, ISyncerCompensator syncerCompensator) {
        this.replication = replication;
        this.filterChain = filterChain;
        this.queue = queue;
        this.taskId = taskId;
        this.status = status;
        this.syncerCompensator = syncerCompensator;
        ThreadPoolUtils.exec(new SendCommandTask.AliveMonitorThread());
    }

    @Override
    public void run() {

        while (true){
            try {

                KeyValueEventEntity keyValueEventEntity=null;
                keyValueEventEntity=queue.take();
                keyValueEventEntity.setISyncerCompensator(syncerCompensator);
//                System.out.println(JSON.toJSONString(queue.take()));
                try {


                    if(null!=keyValueEventEntity){
                        filterChain.run(replication,keyValueEventEntity);
                    }

                }catch (Exception e){
                    log.warn("[{}]抛弃key:{}:原因[{}]",taskId,JSON.toJSONString(keyValueEventEntity.getEvent()),e.getMessage());
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

    class AliveMonitorThread implements Runnable{

        @Override
        public void run() {
            while (true){
                if (SingleTaskDataManagerUtils.isTaskClose(taskId)) {
                    int i=3;
                    if(i>=0&&queue.isEmpty()){
                        i--;
                        return;
                    }
                    if(i<0&&queue.isEmpty()){
                        //判断任务是否关闭
                        try {
                            replication.close();
                            if (status) {
                                Thread.currentThread().interrupt();
                                status = false;
                                System.out.println(" 线程正准备关闭..." + Thread.currentThread().getName());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }


}


