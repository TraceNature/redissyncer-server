package syncer.syncerplusredis.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;
import syncer.syncerplusredis.model.DataMonitorModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/27
 */
//@Component
@Mapper
public interface DataMonitorMapper {
    // 根据 ID 查询
    @Select("SELECT * FROM t_data_monitoring")
    List<DataMonitorModel> selectAll()throws Exception;

    @Select("SELECT * FROM t_data_monitoring WHERE taskId =#{taskId}")
    List<DataMonitorModel> findDataMonitorModelListByTaskId(@Param("taskId") String taskId)throws Exception;

    @Select("SELECT * FROM t_data_monitoring WHERE groupId =#{groupId}")
    List<DataMonitorModel> findDataMonitorModelListByGroupId(@Param("groupId") String groupId)throws Exception;

    @Insert("INSERT INTO t_data_monitoring(allKeyCount,hashCount,stringCount,listCount,setCount,zSetCount,idempotentCount,dataCompensationCount,abandonCount,taskId,groupId ) VALUES(#{allKeyCount},#{hashCount},#{stringCount},#{listCount},#{setCount},#{zSetCount},#{idempotentCount},#{dataCompensationCount},#{abandonCount},#{taskId},#{groupId} )")
    boolean insertDataMonitorModelModel(DataMonitorModel dataMonitorModel)throws Exception;

    @Delete("DELETE FROM t_data_monitoring WHERE id=#{id}")
    void deleteDataMonitorModelById(@Param("id")String id)throws Exception;

    @Delete("DELETE FROM t_data_monitoring WHERE taskId=#{taskId}")
    void deleteDataMonitorModelByTaskId(@Param("taskId")String taskId)throws Exception;

    @Delete("DELETE FROM t_data_monitoring WHERE groupId=#{groupId}")
    void deleteDataMonitorModelByGroupId(@Param("groupId")String groupId)throws Exception;
}
