package syncer.syncerservice.compensator;

import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.queue.LocalMemoryQueue;
import syncer.syncerservice.util.queue.SyncerQueue;

/**
 * 多线程补偿
 */
public class MultiThreadSyncerCompensator {

   private SyncerQueue<KeyValueEventEntity> queue;
    private  String taskId;

    public MultiThreadSyncerCompensator(SyncerQueue<KeyValueEventEntity> queue, String taskId) {
        this.queue = queue;
        this.taskId = taskId;
         this.queue= new LocalMemoryQueue<>(taskId, 1000);
    }



}

