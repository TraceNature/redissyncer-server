package syncer.syncerplusredis.dao;

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
}
