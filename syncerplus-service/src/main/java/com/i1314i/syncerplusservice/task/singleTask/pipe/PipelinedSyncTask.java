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
    private LockPipe lockPipe;
    public PipelinedSyncTask(Pipeline pipelined, SyncTaskEntity taskEntity,LockPipe lockPipe) {
        this.pipelined = pipelined;
        this.taskEntity = taskEntity;
        this.lockPipe=lockPipe;
    }

    @Override
    public Object call() throws Exception {
        while (pipelined!=null){


            lockPipe.syncpipe(pipelined,taskEntity,1,false);

            Thread.sleep(3000);
        }
        return null;
    }
}
