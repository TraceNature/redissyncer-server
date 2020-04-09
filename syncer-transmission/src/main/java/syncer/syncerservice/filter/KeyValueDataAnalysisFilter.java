package syncer.syncerservice.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.util.common.ConstUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.PostRdbSyncEvent;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.datatype.DataType;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TimeUtils;
import syncer.syncerservice.constant.RedisDataTypeAnalysisConstant;
import syncer.syncerservice.exception.FilterNodeException;
import syncer.syncerservice.po.KeyValueEventEntity;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;
import syncer.syncerservice.util.common.Strings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description kv数据(类型、大key)分析节点
 * @Date 2020/1/7
 */
@Slf4j
@Builder
public class KeyValueDataAnalysisFilter implements CommonFilter{
    private Map<String,Long>analysisMap=new ConcurrentHashMap<>();
    private CommonFilter next;
    private JDRedisClient client;
    private String taskId;
    private Long size=0L;

    public KeyValueDataAnalysisFilter(Map<String, Long> analysisMap, CommonFilter next, JDRedisClient client, String taskId) {
        this.analysisMap = new ConcurrentHashMap<>();
        this.next = next;
        this.client = client;
        this.taskId = taskId;
    }

    public KeyValueDataAnalysisFilter(Map<String, Long> analysisMap, CommonFilter next, JDRedisClient client, String taskId, Long size) {
        this.analysisMap = new ConcurrentHashMap<>();
        this.next = next;
        this.client = client;
        this.taskId = taskId;
        this.size = 0L;
    }

    @Override
    public void run(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {

        try {
        Event event=eventEntity.getEvent();
            TaskDataEntity dataEntity= TaskDataManagerUtils.get(taskId);
        //全量同步结束
        if (event instanceof PostRdbSyncEvent) {
            try {
                TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
                analysisMap.put("time", TimeUtils.getNowTimeMills());
                taskMapper.updateDataAnalysis(taskId,JSON.toJSONString(analysisMap));
            }catch (Exception e){
                log.info("全量数据分析报告入库失败");
            }


            log.warn("[{}]全量数据结构分析报告：\r\n{}",taskId, JSON.toJSONString(analysisMap, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat));
        }

        if (event instanceof DumpKeyValuePair) {
            dataEntity.getAllKeyCount().incrementAndGet();
            DumpKeyValuePair dumpKeyValuePair= (DumpKeyValuePair) event;
            addAnalysisMap(dumpKeyValuePair.getDataType());
        }


        if (event instanceof BatchedKeyValuePair<?, ?>) {
            BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
            if(batchedKeyValuePair.isLast()){
                addAnalysisMap(batchedKeyValuePair.getDataType());
            }

            //计算分片数量
            if(batchedKeyValuePair.getBatch()==0){

                if(!StringUtils.isEmpty(batchedKeyValuePair.getKey())){
                    dataEntity.getAllKeyCount().incrementAndGet();
                    log.warn("大key统计：{}", Strings.toString(batchedKeyValuePair.getKey()));
                }

                addAnalysisMap(DataType.FRAGMENTATION);
                addAnalysisMap(DataType.FRAGMENTATION_NUM);
            }else {

                addAnalysisMap(DataType.FRAGMENTATION_NUM);
            }


        }


            //增量数据
            if (event instanceof DefaultCommand) {
                dataEntity.getAllKeyCount().incrementAndGet();
            }

        //继续执行下一Filter节点
        toNext(replicator,eventEntity);

        }catch (Exception e){
            if(analysisMap.containsKey(RedisDataTypeAnalysisConstant.EROR_KEY)){
                analysisMap.put(RedisDataTypeAnalysisConstant.EROR_KEY,analysisMap.get(RedisDataTypeAnalysisConstant.EROR_KEY)+1);
            }else {
                analysisMap.put(RedisDataTypeAnalysisConstant.EROR_KEY,1L);
            }
            throw new FilterNodeException(e.getMessage()+"->KeyValueDataAnalysisFilter",e.getCause());
        }
    }

    @Override
    public void toNext(Replicator replicator, KeyValueEventEntity eventEntity) throws FilterNodeException {
        if(null!=next) {
            next.run(replicator,eventEntity);
        }
    }

    @Override
    public void setNext(CommonFilter nextFilter) {
        this.next=nextFilter;
    }


    void addAnalysisMap(DataType dataType){
        if(null==dataType){
            return;
        }


        try {
            if(analysisMap.containsKey(dataType.toString())){
                analysisMap.put(dataType.toString(),analysisMap.get(dataType.toString())+1);

            }else {
                analysisMap.put(dataType.toString(),1L);
            }


            if(dataType!=null&&dataType.equals(DataType.FRAGMENTATION_NUM)){
                if(analysisMap.containsKey(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM)){
                    analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM)+1);
                }else {
                    analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,1L);
                }
            }



//            if(analysisMap.containsKey(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM)){
//                analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM)+1);
//            }else {
//                analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_FRAGMENTATION_SUM,1L);
//            }



            if(analysisMap.containsKey(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM)){
                if(!dataType.equals(DataType.FRAGMENTATION)&&!dataType.equals(DataType.FRAGMENTATION_NUM)){
                    analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM,analysisMap.get(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM)+1);
                }
            }else {
                if(!dataType.equals(DataType.FRAGMENTATION)&&!dataType.equals(DataType.FRAGMENTATION_NUM)){
                    analysisMap.put(RedisDataTypeAnalysisConstant.KEY_VALUE_SUM,1L);
                }

            }
        }catch (Exception e){
            log.warn("数据分析节点数据计算出现错误[不影响下个节点数据迁移]{}",e.getMessage()+dataType);
        }

    }



}
