package syncer.syncerplusredis.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;
import syncer.syncerplusredis.model.TaskModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/10
 */
@Component
@Mapper
public interface TaskMapper {
    // 根据 ID 查询
    @Select("SELECT * FROM t_task")
    List<TaskModel> selectAll()throws Exception;

    @Select("SELECT * FROM t_task WHERE id =#{id}")
    TaskModel findTaskById(@Param("id") String id)throws Exception;

    @Select("SELECT * FROM t_task WHERE groupId =#{groupId}")
    List<TaskModel>findTaskByGroupId(@Param("groupId")String groupId)throws  Exception;

    @Insert("INSERT INTO t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status,redisVersion,rdbVersion,sourceRedisType,targetRedisType,syncType) VALUES(#{id}, #{groupId},#{taskName},#{sourceRedisAddress},#{sourcePassword},#{targetRedisAddress},#{targetPassword},#{autostart},#{afresh},#{batchSize},#{tasktype},#{offsetPlace},#{taskMsg},#{offset},#{status},#{redisVersion},#{rdbVersion},#{sourceRedisType},#{targetRedisType},#{syncType})")
    boolean insertTask(TaskModel taskModel)throws Exception;


    @Insert({
            "<script>",
            "insert into t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status,redisVersion,rdbVersion,sourceRedisType,targetRedisType,syncType) values ",
            "<foreach collection='taskModelList' item='item' index='index' separator=','>",
            "(#{item.id}, #{item.groupId}, #{item.taskName}, #{item.sourceRedisAddress}, #{item.sourcePassword}, #{item.targetRedisAddress}, #{item.targetPassword}, #{item.autostart}, #{item.afresh}, #{item.batchSize}, #{item.tasktype}, #{item.offsetPlace}, #{item.taskMsg}, #{item.offset}, #{item.status},#{redisVersion},#{rdbVersion},#{sourceRedisType},#{targetRedisType},#{syncType})",
            "</foreach>",
            "</script>"
    })
    int insertTaskList(@Param(value="taskModelList") List<TaskModel> taskModelList);


    @Delete("DELETE FROM t_task WHERE id=#{id}")
    boolean deleteTaskById(@Param("id")String id)throws Exception;

    @Delete("DELETE FROM t_task WHERE groupId=#{groupId}")
    int deleteTasksByGroupId(@Param("groupId") String groupId)throws Exception;

    @Delete("DELETE FROM t_task")
    int deleteAllTask();

    @Update("UPDATE t_task SET groupId=#{groupId} ,taskName=#{taskName},sourceRedisAddress=#{sourceRedisAddress},sourcePassword=#{sourcePassword},targetRedisAddress=#{targetRedisAddress},targetPassword=#{targetPassword},autostart=#{autostart},afresh=#{afresh},batchSize=#{batchSize},tasktype=#{tasktype},offsetPlace=#{offsetPlace},taskMsg=#{taskMsg},offset=#{offset},status=#{status},redisVersion=#{redisVersion},rdbVersion=#{rdbVersion} ,targetRedisType=#{targetRedisType},sourceRedisType=#{sourceRedisType},syncType=#{syncType} WHERE id=#{id}")
    boolean updateTask(TaskModel taskModel)throws Exception;

    @Update("UPDATE t_task SET status=#{status} WHERE id=#{id}")
    boolean updateTaskStatusById(@Param("id")String id,@Param("status")int status)throws Exception;

    @Update("UPDATE t_task SET status=#{status} WHERE groupId=#{groupId}")
    boolean updateTaskStausByGroupId(@Param("groupId")String groupId,@Param("status")int status)throws Exception;


    @Update("UPDATE t_task SET offset=#{offset} WHERE id=#{id}")
    boolean updateTaskOffsetById(@Param("id")String id,@Param("offset")long offset)throws Exception;


//    @Update({
//            "<script>",
//            "<foreach collection='taskModelList' item='item' index='index' >",
//            "UPDATE t_task SET groupId=#{item.groupId} ,taskName=#{item.taskName},sourceRedisAddress=#{item.sourceRedisAddress},sourcePassword=#{item.sourcePassword},targetRedisAddress=#{item.targetRedisAddress},targetPassword=#{item.targetPassword},autostart=#{item.autostart},afresh=#{item.afresh},batchSize=#{item.batchSize},tasktype=#{item.tasktype},offsetPlace=#{item.offsetPlace},taskMsg=#{item.taskMsg},offset=#{item.offset},status=#{item.status} WHERE id=#{item.id};",
//            "</foreach>",
//            "</script>"
//    })

//        @Update({
//                "<script>",
//                "<foreach collection='taskModelList' item='item' index='index' open='' close='' separator=';/r/n'>",
//                "UPDATE t_task SET <if test='item.groupId!=null'> groupId=#{item.groupId} , </if>  <if test='item.taskName!=null'>taskName=#{item.taskName},</if> <if test='item.sourceRedisAddress!=null'>sourceRedisAddress=#{item.sourceRedisAddress},</if> <if test='item.sourcePassword!=null'> sourcePassword=#{item.sourcePassword}, </if>  <if test='item.targetRedisAddress!=null'> targetRedisAddress=#{item.targetRedisAddress}, </if>  <if test='item.targetPassword!=null'> targetPassword=#{item.targetPassword},  </if>   <if test='item.autostart!=null'>  autostart=#{item.autostart}, </if> <if test='item.afresh!=null'>  afresh=#{item.afresh}, </if>  <if test='item.batchSize!=null'> batchSize=#{item.batchSize}, </if> <if test='item.tasktype!=null'> tasktype=#{item.tasktype}, </if> <if test='item.offsetPlace!=null'> offsetPlace=#{item.offsetPlace}, </if> <if test='item.taskMsg!=null'> taskMsg=#{item.taskMsg}, </if>  <if test='item.offset!=null'> offset=#{item.offset},</if>  <if test='item.status!=null'> status=#{item.status} </if> WHERE id=#{item.id}",
//                "</foreach>",
//                "</script>"
//        })
//        Integer  updateTaskList(@Param(value="taskModelList")List<TaskModel>taskModelList)throws Exception;

}
