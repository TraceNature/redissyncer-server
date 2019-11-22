package syncer.syncerplusservice.task.singleTask.pipe.cluster;

import syncer.syncerplusredis.entity.SyncTaskEntity;
import syncer.syncerplusservice.util.Jedis.cluster.pipelineCluster.JedisClusterPipeline;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * 管道提交检测线程
 */
@Slf4j
public class PipelinedClusterSyncTask implements Callable<Object> {
    private JedisClusterPipeline pipelined;
    private SyncTaskEntity taskEntity;
    private LockPipeCluster lockPipe;
    public PipelinedClusterSyncTask(JedisClusterPipeline pipelined, SyncTaskEntity taskEntity, LockPipeCluster lockPipe) {
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
