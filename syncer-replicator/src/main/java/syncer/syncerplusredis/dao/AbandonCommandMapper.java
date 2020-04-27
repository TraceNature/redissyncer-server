package syncer.syncerplusredis.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import syncer.syncerplusredis.model.AbandonCommandModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 抛弃命令记录
 * @Date 2020/4/26
 */
@Component
@Mapper
public interface AbandonCommandMapper {
    // 根据 ID 查询
    @Select("SELECT * FROM t_abandon_command")
    List<AbandonCommandModel> selectAll()throws Exception;

    @Select("SELECT * FROM t_abandon_command WHERE taskId =#{taskId}")
    List<AbandonCommandModel> findAbandonCommandListByTaskId(@Param("taskId") String taskId)throws Exception;

    @Select("SELECT * FROM t_abandon_command WHERE groupId =#{groupId}")
    List<AbandonCommandModel> findAbandonCommandListByGroupId(@Param("groupId") String groupId)throws Exception;


}
