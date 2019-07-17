package com.i1314i.syncerplusservice.task;

import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Pipeline;

import java.util.concurrent.Callable;

/**
 * 管道提交检测线程
 */
@Slf4j
public class PipelinedSyncTask implements Callable<Object> {
    private Pipeline pipelined;
    private SyncTaskEntity taskEntity;

    public PipelinedSyncTask(Pipeline pipelined, SyncTaskEntity taskEntity) {
        this.pipelined = pipelined;
        this.taskEntity = taskEntity;
    }

    @Override
    public Object call() throws Exception {
        while (pipelined!=null){
            Thread.sleep(30000);

            pipelined.sync();
            log.info("将管道中超过 "+taskEntity.getSyncNums()+"个值提交");
            taskEntity.clear();

        }
        return null;
    }
}
