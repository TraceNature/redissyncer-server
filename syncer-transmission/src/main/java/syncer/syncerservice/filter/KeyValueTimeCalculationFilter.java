package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.KeyWeed0utException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

/**
 * 全量kv剩余过期时间计算节点
 */
@Builder
@Getter
@Setter
public class KeyValueTimeCalculationFilter implements CommonFilter  {
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;


    public KeyValueTimeCalculationFilter(CommonFilter next, JDRedisClient client, String taskId) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) {
        Event event=eventEntity.getEvent();
        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
            Long time=dumpKeyValuePair.getExpiredMs();
            try {
                timeCalculation(eventEntity,time);
            } catch (KeyWeed0utException e) {
                //抛弃此kv
                return;
            }
        }

        if (event instanceof BatchedKeyValuePair<?, ?>) {
            BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
            Long time=batchedKeyValuePair.getExpiredMs();
            try {
                timeCalculation(eventEntity,time);
            } catch (KeyWeed0utException e) {
                //抛弃此kv
                return;
            }
        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) {
        if(null!=next){
            next.run(replicator,eventEntity);

        }

    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }

    void timeCalculation(KeyValueEventEntity eventEntity,Long time) throws KeyWeed0utException {
        Long ms=0L;
        if (null==time) {
            ms = 0L;
        } else {
            ms = time - System.currentTimeMillis();
            if(ms<0L){
                //key已经过期 忽略本key
                throw new KeyWeed0utException("key过期被抛弃");
            }
        }
        eventEntity.setMs(ms);
    }
}
