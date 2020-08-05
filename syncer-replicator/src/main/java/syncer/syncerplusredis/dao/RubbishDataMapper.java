package syncer.syncerplusredis.dao;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zhanenqiang
 * @Description 垃圾数据清理
 * @Date 2020/7/27
 */
@Mapper
public interface RubbishDataMapper {
    @Delete("DELETE  FROM t_task_offset WHERE taskId NOT IN (SELECT id FROM t_task)")
    void  deleteRubbishDataFromTaskOffSet();

    @Delete("DELETE  FROM t_big_key WHERE taskId NOT IN (SELECT id FROM t_task)")
    void  deleteRubbishDataFromTaskBigKey();


    @Delete("DELETE  FROM t_data_monitoring WHERE taskId NOT IN (SELECT id FROM t_task)")
    void  deleteRubbishDataFromTaskDataMonitor();


    @Delete("DELETE  FROM t_data_compensation WHERE taskId NOT IN (SELECT id FROM t_task)")
    void  deleteRubbishDataFromTaskDataCompensation();

    @Delete("DELETE  FROM t_abandon_command WHERE taskId NOT IN (SELECT id FROM t_task)")
    void  deleteRubbishDataFromTaskDataAbandonCommand();
}
