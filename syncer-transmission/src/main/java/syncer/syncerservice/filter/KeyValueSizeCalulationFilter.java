package syncer.syncerservice.filter;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.exception.KeyWeed0utException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.Arrays;

/**
 * @author zhanenqiang
 * @Description 数据总量计算节点
 * @Date 2020/1/15
 */

@Builder
@Getter
@Setter
@Slf4j
public class KeyValueSizeCalulationFilter implements CommonFilter {
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;
    private Long size;

    public KeyValueSizeCalulationFilter(CommonFilter next, JDRedisClient client, String taskId, Long size) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.size = size;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {

        //DUMP格式数据
        Event event=eventEntity.getEvent();
        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;

            if(null==dumpKeyValuePair.getValue()
                    ||null==dumpKeyValuePair.getKey()){
                return;
            }

//            size+=dumpKeyValuePair.getSize();
//            System.out.println("dumpSize: "+size);
            DB db = dumpKeyValuePair.getDb();

        }

        //分批格式数据
        if (event instanceof BatchedKeyValuePair<?, ?>) {
            BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;

            if((batchedKeyValuePair.getBatch()==0
                    &&null==batchedKeyValuePair.getValue())
                    ||null==batchedKeyValuePair.getValue()){

                return;
            }
//            size+=batchedKeyValuePair.getSize();
//            System.out.println("dumpSize: "+size);



        }

        //增量数据
        if (event instanceof DefaultCommand) {
            DefaultCommand defaultCommand = (DefaultCommand) event;
            if(Arrays.equals(defaultCommand.getCommand(),"SELECT".getBytes())) {
                int commDbNum = Integer.parseInt(new String(defaultCommand.getArgs()[0]));


            }

        }



        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {
        if(null!=next){
            next.run(replicator,eventEntity);

        }
    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }


}
