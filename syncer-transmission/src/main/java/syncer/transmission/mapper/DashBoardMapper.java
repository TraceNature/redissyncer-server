package syncer.transmission.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/6
 */
@Mapper
public interface DashBoardMapper {
    @Select("select count(1) from t_task limit 1 ")
    int taskCount();


    @Select("select count(1) from t_task where status=5 limit 1 ")
    int brokenCount();

    @Select("select count(1) from t_task where status=0 limit 1 ")
    int stopCount();

    @Select("select count(1) from t_task where status=1 or status=2 or status=3 or status=6 or status=7 limit 1 ")
    int runCount();

    @Select("select count(1) from t_task where syncType=1 limit 1 ")
    int syncCount();

    @Select("select count(1) from t_task where syncType=2 limit 1 ")
    int rdbCount();

    @Select("select count(1) from t_task where syncType=3 limit 1 ")
    int aofCount();

    @Select("select count(1) from t_task where syncType=4 limit 1 ")
    int mixedCount();

    @Select("select count(1) from t_task where syncType=5 limit 1 ")
    int onlineRdbCount();

    @Select("select count(1) from t_task where syncType=6 limit 1 ")
    int onlineAofCount();

    @Select("select count(1) from t_task where syncType=7 limit 1 ")
    int onlineMixedCount();

    @Select("select count(1) from t_task where syncType=8 limit 1 ")
    int commandDumpUpCount();
}
