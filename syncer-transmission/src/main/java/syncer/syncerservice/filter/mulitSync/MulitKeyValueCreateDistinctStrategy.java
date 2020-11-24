package syncer.syncerservice.filter.mulitSync;


import lombok.AllArgsConstructor;
import lombok.Builder;
import syncer.syncerpluscommon.constant.ResultCodeAndMessage;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.exception.FilterNodeException;

/**
 * @author zhanenqiang
 * @Description 双向同步创建校验不重复策略
 * @Date 2020/11/13
 */
@Builder
@AllArgsConstructor
public class MulitKeyValueCreateDistinctStrategy implements IMulitCommonStrategy {
    private IMulitCommonStrategy next;
    private MuiltCreateTaskData data;
    private ParentMultiTaskModel parentMultiTaskModel;


    @Override
    public void run(MuiltCreateTaskData data, ParentMultiTaskModel parentMultiTaskModel) throws Exception {
        if (MultiSyncTaskManagerutils.containsValue(data)) {
            throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_TASKSETTING_ERROR.getCode(), ResultCodeAndMessage.TASK_MSG_TASKSETTING_ERROR.getMsg()));
        }
        //继续执行下一Filter节点
        toNext(data, parentMultiTaskModel);
    }

    @Override
    public void toNext(MuiltCreateTaskData data, ParentMultiTaskModel parentMultiTaskModel) throws Exception {
        if (null != next) {
            next.run(data, parentMultiTaskModel);
        }
    }

    @Override
    public void setNext(IMulitCommonStrategy nextFilter) {
        this.next = nextFilter;
    }
}

