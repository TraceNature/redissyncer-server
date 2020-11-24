package syncer.syncerservice.filter.mulitSync;

import com.beust.jcommander.internal.Lists;
import lombok.Builder;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/11/16
 */

@Builder
public class MulitCreateDistinctStrategyFactory implements IMulitStrategyFactory {
    @Override
    public List<IMulitCommonStrategy> getStrategyList(MuiltCreateTaskData data, ParentMultiTaskModel parentMultiTaskModel) {

        List<IMulitCommonStrategy>startCheckBaseStrategyList= Lists.newArrayList();
        //判断是否重复
        startCheckBaseStrategyList.add(MulitKeyValueCreateDistinctStrategy.builder().data(data).parentMultiTaskModel(parentMultiTaskModel).build());

        return startCheckBaseStrategyList;
    }
}
