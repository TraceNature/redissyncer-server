package syncer.syncerservice.service;

import org.apache.ibatis.annotations.Param;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerplusredis.model.RdbVersionModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description RDB版本映射
 * @Date 2020/4/22
 */
public interface IRdbVersionService {
    PageBean<RdbVersionModel> findRdbVersionModelByPage(int currentPage, int pageSize) throws Exception;
     List<RdbVersionModel>selectAll()throws Exception;
    boolean deleteRdbVersionModelById(Integer id)throws Exception;
    RdbVersionModel findRdbVersionModelById(Integer id)throws Exception;
    RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion,Integer rdbVersion)throws Exception;
    boolean insertRdbVersionModel(RdbVersionModel rdbVersionModel)throws Exception;
    RdbVersionModel updateRdbVersionModelById(Integer id, String redisVersion,Integer rdbVersion)throws Exception;
}
