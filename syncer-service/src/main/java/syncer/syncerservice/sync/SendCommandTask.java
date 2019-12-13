package syncer.syncerservice.sync;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.filter.KeyValueRunFilterChain;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.queue.SyncerQueue;

import java.io.IOException;

@Slf4j
@Builder
public class SendCommandTask implements Runnable{
    private Replicator r;
    private KeyValueRunFilterChain filterChain;
    private SyncerQueue<KeyValueEventEntity> queue;
    private String taskId;
    private boolean status = true;

    public SendCommandTask(Replicator r, KeyValueRunFilterChain filterChain, SyncerQueue<KeyValueEventEntity> queue, String taskId, boolean status) {
        this.r = r;
        this.filterChain = filterChain;
        this.queue = queue;
        this.taskId = taskId;
        this.status = status;
    }

    @Override
    public void run() {

        while (true){
            try {
                if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {
                    //判断任务是否关闭
                    try {
                        r.close();
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
//                System.out.println(JSON.toJSONString(queue.take()));
                filterChain.run(r,queue.take());
            }catch (Exception e){
                try {
                    log.warn("[{}]key从队列拿出失败:{}",taskId,e.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }


    }



}
