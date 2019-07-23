package com.i1314i.syncerplusservice.task.singleTask.pipe;

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

            if(taskEntity.getSyncNums()>0){
                pipelined.sync();
                log.info("将管道中超过 {}个值提交",taskEntity.getSyncNums());
                taskEntity.clear();
            }

        }
        return null;
    }
}
