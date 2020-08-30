package syncer.syncerservice.sync;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusredis.dao.AbandonCommandMapper;
import syncer.syncerplusredis.entity.TaskOffsetEntity;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.model.AbandonCommandModel;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.DataTypeUtils;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskErrorUtils;
import syncer.syncerservice.compensator.ISyncerCompensator;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.filter.KeyValueRunFilterChain;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.DataCleanUtils;
import syncer.syncerservice.util.RedisCommandTypeUtils;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.taskutil.TaskGetUtils;
import syncer.syncerservice.util.taskutil.taskServiceQueue.DbDataCommitQueue;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/9
 */

@Slf4j
@Builder
public class SendCommandWithOutQueue {
    private Replicator r;
    private KeyValueRunFilterChain filterChain;
    private String taskId;
    private boolean status = true;
    private ISyncerCompensator syncerCompensator;


    public SendCommandWithOutQueue(Replicator r, KeyValueRunFilterChain filterChain, String taskId, boolean status, ISyncerCompensator syncerCompensator) {
        this.r = r;
        this.filterChain = filterChain;
        this.taskId = taskId;
        this.status  = true;
        this.syncerCompensator = syncerCompensator;
    }

    void run(KeyValueEventEntity keyValueEventEntity){


            try {
                keyValueEventEntity.setISyncerCompensator(syncerCompensator);
                if(null!=keyValueEventEntity){
                    filterChain.run(r,keyValueEventEntity);
                }

                DataCleanUtils.cleanData(keyValueEventEntity);


//                throw new FilterNodeException("测试异常");
            }catch (Exception e){
                Event event=keyValueEventEntity.getEvent();
                String keyName=null;
                String command=null;
                String value=null;
                int dataType=12;
                long ttl=-1L;
                if(event instanceof DefaultCommand){
                    DefaultCommand defaultCommand= (DefaultCommand) event;
                    command=Strings.byteToString(defaultCommand.getCommand());
                    if(defaultCommand.getArgs().length>0){
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand())+Strings.byteToString(((DefaultCommand) event).getArgs()[0]);
                    }else{
                        keyName= Strings.byteToString(((DefaultCommand) event).getCommand());
                    }

                }else if(event instanceof DumpKeyValuePair){
                    DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
                    keyName= Strings.byteToString(dumpKeyValuePair.getKey());
                    command="RestoreReplace";
                    if(dumpKeyValuePair.getExpiredMs()!=null){
                        ttl=dumpKeyValuePair.getExpiredMs();
                    }
                    dataType= DataTypeUtils.getType(dumpKeyValuePair.getDataType());
                }else if(event instanceof BatchedKeyValuePair){
                    BatchedKeyValuePair batchedKeyValuePair= (BatchedKeyValuePair) event;
                    keyName=Strings.toString(batchedKeyValuePair.getKey());
                    RedisCommandTypeEnum typeEnum= RedisCommandTypeUtils.getRedisCommandTypeEnum(batchedKeyValuePair.getValueRdbType());
                    command=typeEnum.name();
                    if(batchedKeyValuePair.getExpiredMs()!=null){
                        ttl=batchedKeyValuePair.getExpiredMs();
                    }
                    dataType= DataTypeUtils.getType(batchedKeyValuePair.getDataType());
                }

                if(StringUtils.isEmpty(command)){
                    return;
                }

                //写入数据库抛弃key
                try {
                    if(keyName==null){
                        keyName="";
                    }
                    String message;
                    if(e.getMessage()==null){
                        message="";
                    }else{
                        message=e.getMessage();
                    }
                    String desc;
                    if(event==null){
                        desc="";

                    }else {
                        desc=event.getClass().toString();
                    }


                    SqliteOPUtils.insertSimpleAbandonCommandModel(AbandonCommandModel
                            .builder()
                            .command(command)
                            .exception(message)
                            .key(keyName)
                            .taskId(taskId)
                            .type(dataType)
                            .desc(desc)
                            .ttl(ttl)
                            .groupId(TaskGetUtils.getRunningTaskGroupId(taskId))
                            .build());
                }catch (Exception ez){

                    log.error("[{}]抛弃key:{}信息写入数据库失败,原因[{}]",taskId, keyName,ez.getMessage());
                    ez.printStackTrace();
                }
                long errorCount=TaskDataManagerUtils.get(taskId).getTaskModel().getErrorCount();
                if (errorCount >= 0) {
//                    long error = errorNums.incrementAndGet();

                    long error= TaskDataManagerUtils.get(taskId).getErrorNums().incrementAndGet();
                    if (error >= errorCount) {
                        TaskErrorUtils.brokenStatusAndLog("被抛弃key数量到达阈值[" + errorCount + "],exception reason["+e.getMessage()+"]", this.getClass(), taskId);
                    }
                }
                log.error("[{}]抛弃key:{} ,class:[{}]:原因[{}]",taskId, keyName,event.getClass().toString(),e.getMessage());
                DataCleanUtils.cleanData(keyValueEventEntity,event);
                e.printStackTrace();
            }
    }
}
