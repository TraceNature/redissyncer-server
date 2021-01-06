package syncer.transmission.mapper;

import org.apache.ibatis.annotations.*;
import syncer.transmission.model.AbandonCommandModel;


import java.util.List;

/**
 * @author zhanenqiang
 * @Description 抛弃命令记录
 * @Date 2020/4/26
 */
//@Component
@Mapper
public interface AbandonCommandMapper {
    // 根据 ID 查询
    @Select("SELECT * FROM t_abandon_command")
    List<AbandonCommandModel> selectAll()throws Exception;

    @Select("SELECT * FROM t_abandon_command WHERE taskId =#{taskId}")
    List<AbandonCommandModel> findAbandonCommandListByTaskId(@Param("taskId") String taskId)throws Exception;

    @Select("SELECT * FROM t_abandon_command WHERE groupId =#{groupId}")
    List<AbandonCommandModel> findAbandonCommandListByGroupId(@Param("groupId") String groupId)throws Exception;

    @Insert("INSERT INTO t_abandon_command(taskId,groupId,command,key,value,type,ttl,exception,result,desc) VALUES(#{taskId},#{groupId},#{command},#{key},#{value},#{type},#{ttl},#{exception},#{result},#{desc})")
    boolean insertAbandonCommandModel(AbandonCommandModel abandonCommandModel)throws Exception;

    @Insert("INSERT INTO t_abandon_command(taskId,command,key,type,exception,desc,ttl,groupId) VALUES(#{taskId},#{command},#{key},#{type},#{exception},#{desc},#{ttl},#{groupId})")
    boolean insertSimpleAbandonCommandModel(AbandonCommandModel abandonCommandModel)throws Exception;

    @Delete("DELETE FROM t_abandon_command WHERE id=#{id}")
    void deleteAbandonCommandModelById(@Param("id") String id)throws Exception;

    @Delete("DELETE FROM t_abandon_command WHERE taskId=#{taskId}")
    void deleteAbandonCommandModelByTaskId(@Param("taskId") String taskId)throws Exception;

    @Delete("DELETE FROM t_abandon_command WHERE groupId=#{groupId}")
    void deleteAbandonCommandModelByGroupId(@Param("groupId") String groupId)throws Exception;

}
