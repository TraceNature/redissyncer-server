package syncer.syncerservice.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.exception.KeyWeed0utException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.util.Arrays;

/**
 * db映射关系Filter
 */
@Builder
@Getter
@Setter
public class KeyValueEventDBMappingFilter implements CommonFilter {
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;

    public KeyValueEventDBMappingFilter(CommonFilter next, JDRedisClient client, String taskId) {
        this.next = next;
        this.client = client;
        this.taskId = taskId;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) {

        //DUMP格式数据
        Event event=eventEntity.getEvent();
        if (event instanceof DumpKeyValuePair) {
            DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
            DB db = dumpKeyValuePair.getDb();
            try {
                dbMapping(eventEntity,db);
            } catch (KeyWeed0utException e) {
                //抛弃此kv
                return;
            }
        }

        //分批格式数据
        if (event instanceof BatchedKeyValuePair<?, ?>) {
            BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
            DB db = batchedKeyValuePair.getDb();
            try {
                dbMapping(eventEntity,db);
            } catch (KeyWeed0utException e) {
                //抛弃此kv
                return;
            }
        }

        //增量数据
        if (event instanceof DefaultCommand) {
            DefaultCommand defaultCommand = (DefaultCommand) event;
            if(Arrays.equals(defaultCommand.getCommand(),"SELECT".getBytes())) {
                int commDbNum = Integer.parseInt(new String(defaultCommand.getArgs()[0]));
                try {
                    commanddbMapping(eventEntity,commDbNum,defaultCommand);
                } catch (KeyWeed0utException e) {
                    //抛弃此kv
                    return;
                }
            }

        }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) {
        if(null!=next)
            next.run(replicator,eventEntity);
    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }

    /**
     * 全量KV DBNUM映射
     * @param eventEntity
     * @param db
     * @throws KeyWeed0utException
     */
    void dbMapping(KeyValueEventEntity eventEntity,DB db) throws KeyWeed0utException {

        long dbbnum=db.getDbNumber();
        if (null != eventEntity.getDbMapper() && eventEntity.getDbMapper().size() > 0) {
            if (eventEntity.getDbMapper().containsKey((int) db.getDbNumber())) {
                dbbnum = eventEntity.getDbMapper().get((int) db.getDbNumber());
            } else {
                //忽略本key
                throw new KeyWeed0utException("key抛弃");

            }
        }
        eventEntity.setDbNum(dbbnum);
    }

    /**
     * 增量KV DBNUM映射
     * @param eventEntity
     * @param correntDbNum
     * @throws KeyWeed0utException
     */
    void commanddbMapping(KeyValueEventEntity eventEntity,Integer correntDbNum,DefaultCommand command) throws KeyWeed0utException {
        long dbbnum=correntDbNum;
        if (null != eventEntity.getDbMapper() && eventEntity.getDbMapper().size() > 0) {
            if (eventEntity.getDbMapper().containsKey(dbbnum)) {
                dbbnum = eventEntity.getDbMapper().get(dbbnum);
                byte[][]dbData=command.getArgs();
                dbData[0]=String.valueOf(dbbnum).getBytes();
                command.setArgs(dbData);
            } else {
                //忽略本key
                throw new KeyWeed0utException("key抛弃");
            }
        }
        eventEntity.setEvent(command);
    }
}
