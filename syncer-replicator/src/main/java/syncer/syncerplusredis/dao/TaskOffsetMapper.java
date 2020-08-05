package syncer.syncerplusredis.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;
import syncer.syncerplusredis.entity.TaskOffsetEntity;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */
//@Component
@Mapper
public interface TaskOffsetMapper {
    @Insert("INSERT INTO t_task_offset(taskId,offset,replId) VALUES(#{taskId},#{offset},#{replId})")
    boolean insetTaskOffset(TaskOffsetEntity taskOffsetEntity)throws Exception;

    @Update("UPDATE t_task_offset SET offset=#{offset} WHERE taskId=#{taskId}")
    boolean updateOffsetByTaskId(@Param("taskId")boolean taskId,@Param("offset")long offset)throws Exception;

    @Update("UPDATE t_task_offset SET replId=#{replId} WHERE taskId=#{taskId}")
    boolean updateReplIdByTaskId(@Param("taskId")boolean taskId,@Param("replId")String replId)throws Exception;


    @Update("UPDATE t_task_offset SET replId=#{replId},offset=#{offset} WHERE taskId=#{taskId}")
    boolean updateOffsetAndReplIdByTaskId(@Param("taskId")boolean taskId,@Param("replId")String replId,@Param("offset")long offset)throws Exception;

    @Delete("DELETE FROM t_task_offset WHERE taskId=#{taskId}")
    int delOffsetEntityByTaskId(@Param("taskId")String taskId)throws Exception;

    @Delete("DELETE FROM t_task_offset WHERE groupId=#{groupId}")
    int delOffsetEntityByGroupId(@Param("groupId")String groupId)throws Exception;
}
