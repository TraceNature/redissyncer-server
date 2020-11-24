package syncer.syncerservice.filter.mulitSync;

import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/11/13
 */
public interface IMulitStrategyFactory {
    List<IMulitCommonStrategy> getStrategyList(MuiltCreateTaskData data, ParentMultiTaskModel parentMultiTaskModel);
}
