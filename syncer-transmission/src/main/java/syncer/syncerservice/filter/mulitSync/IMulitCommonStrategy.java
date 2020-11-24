package syncer.syncerservice.filter.mulitSync;

import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;
import syncer.syncerservice.exception.FilterNodeException;

/**
 * @author zhanenqiang
 * @Description 双向校验通用策略
 * @Date 2020/11/13
 */
public interface IMulitCommonStrategy {

    void run(MuiltCreateTaskData data,ParentMultiTaskModel parentMultiTaskModel) throws Exception;

    void toNext(MuiltCreateTaskData data, ParentMultiTaskModel parentMultiTaskModel) throws Exception;

    void setNext(IMulitCommonStrategy nextFilter);
}
