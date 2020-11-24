package syncer.syncerservice.service.muilt;

import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/10/13
 */
public interface MuiltTaskService {
    /**
     * 创建双向同步任务，默认不自动启动
     * @param parentMultiTaskModel
     * @return
     */
    ResultMap createMuilTask(ParentMultiTaskModel parentMultiTaskModel);


    /**
     * 启动双向同步任务
     * @param parentId
     * @return
     */
    ResultMap startMuilTask(String parentId);


    /**
     * 停止双向同步任务
     * @param taskIdList
     * @return
     */
    ResultMap stopMuilTask(List<String> taskIdList);

    ResultMap startTaskByTaskId(String taskId,boolean afresh) throws Exception;
}
