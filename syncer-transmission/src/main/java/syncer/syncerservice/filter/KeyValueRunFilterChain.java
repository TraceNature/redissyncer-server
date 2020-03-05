package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;
import java.util.List;

@Builder
@Getter@Setter
/**
 * 责任链模式
 */
public class KeyValueRunFilterChain {


    //过滤器列表
    private List<CommonFilter>commonFilterList;

    private CommonFilter commonFilter;


    public KeyValueRunFilterChain(List<CommonFilter> commonFilterList, CommonFilter commonFilter) {
        this.commonFilterList = commonFilterList;
        //组装链式结构
        if(commonFilterList!=null&&commonFilterList.size()>0){
            for (int i = 0; i <commonFilterList.size() ; i++) {
                if(i<commonFilterList.size()-1){
                    CommonFilter filter=commonFilterList.get(i);
                    filter.setNext(commonFilterList.get(i+1));
                }
            }
            this.commonFilter=commonFilterList.get(0);
        }
    }


    public void  run(Replicator replicator,KeyValueEventEntity eventEntity) throws FilterNodeException {
        if(null!=commonFilter){
            commonFilter.run(replicator,eventEntity);
        }

    }
}
