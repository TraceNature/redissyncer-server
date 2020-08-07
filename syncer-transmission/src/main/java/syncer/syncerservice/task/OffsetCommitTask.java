package syncer.syncerservice.task;

import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.util.taskutil.taskServiceQueue.DbDataCommitQueue;

import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/4
 */
public class OffsetCommitTask implements Runnable{
    @Override
    public void run() {
        while (true){
            try {
                Map<String, TaskDataEntity> aliveThreadHashMap=TaskDataManagerUtils.getAliveThreadHashMap();

                aliveThreadHashMap.entrySet().forEach(data ->{
                    if(SyncType.SYNC.getCode().equals(data.getValue().getTaskModel().getSyncType())
                            ||SyncType.COMMANDDUMPUP.getCode().equals(data.getValue().getTaskModel().getSyncType())){
                        if(data.getValue().getOffSetEntity().getReplId()!=null&&data.getValue().getOffSetEntity().getReplOffset()!=null){
                            DbDataCommitQueue.put(OffSetCommitEntity.builder().taskId(data.getKey())
                                    .replId(data.getValue().getOffSetEntity().getReplId())
                                    .offset(data.getValue().getOffSetEntity().getReplOffset().get())
                                    .build());


                        }

                    }
                });

                Thread.sleep(1000*60);
            }catch (Exception e){

            }
        }
    }
}
