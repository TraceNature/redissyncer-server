package syncer.transmission.strategy.commandprocessing;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.replication.Replication;
import syncer.transmission.exception.StartegyNodeException;
import syncer.transmission.po.entity.KeyValueEventEntity;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
@Builder
@Getter
@Setter

public class ProcessingRunStrategyChain {
    //过滤器列表
    private List<CommonProcessingStrategy> commonFilterList;

    private CommonProcessingStrategy commonFilter;


    public ProcessingRunStrategyChain(List<CommonProcessingStrategy> commonFilterList, CommonProcessingStrategy strategy) {
        this.commonFilterList = commonFilterList;
        //组装链式结构
        if(commonFilterList!=null&&commonFilterList.size()>0){
            for (int i = 0; i <commonFilterList.size() ; i++) {
                if(i<commonFilterList.size()-1){
                    CommonProcessingStrategy filter=commonFilterList.get(i);
                    filter.setNext(commonFilterList.get(i+1));
                }
            }
            this.commonFilter=commonFilterList.get(0);
        }
    }


    public void  run(Replication replication, KeyValueEventEntity eventEntity) throws StartegyNodeException {
        if(null!=commonFilter){
            commonFilter.run(replication,eventEntity);
        }

    }
}
