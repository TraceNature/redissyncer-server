package syncer.syncerservice.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;
import syncer.syncerservice.model.TaskModel;

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

    @Insert("INSERT INTO t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status) VALUES(#{id}, #{groupId},#{taskName},#{sourceRedisAddress},#{sourcePassword},#{targetRedisAddress},#{targetPassword},#{autostart},#{afresh},#{batchSize},#{tasktype},#{offsetPlace},#{taskMsg},#{offset},#{status})")
    boolean insertTask(TaskModel taskModel)throws Exception;


    @Insert({
            "<script>",
            "insert into t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status) values ",
            "<foreach collection='taskModelList' item='item' index='index' separator=','>",
            "(#{item.id}, #{item.groupId}, #{item.taskName}, #{item.sourceRedisAddress}, #{item.sourcePassword}, #{item.targetRedisAddress}, #{item.targetPassword}, #{item.autostart}, #{item.afresh}, #{item.batchSize}, #{item.tasktype}, #{item.offsetPlace}, #{item.taskMsg}, #{item.offset}, #{item.status})",
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

}
