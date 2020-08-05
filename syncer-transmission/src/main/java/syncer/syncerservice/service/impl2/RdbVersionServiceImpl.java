package syncer.syncerservice.service.impl2;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerplusredis.dao.RdbVersionMapper;
import syncer.syncerplusredis.model.RdbVersionModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerservice.service.IRdbVersionService;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/22
 */
@Service("rdbVersionService")
public class RdbVersionServiceImpl implements IRdbVersionService {


    @Override
    public PageBean<RdbVersionModel> findRdbVersionModelByPage(int currentPage, int pageSize) throws Exception {
        //设置分页信息，分别是当前页数和每页显示的总记录数【记住：必须在mapper接口中的方法执行之前设置该分页信息】
        PageHelper.startPage(currentPage, pageSize);
        List<RdbVersionModel> allItems = SqliteOPUtils.RdbVersionSelectAll();        //全部商品
        int countNums = SqliteOPUtils.RdbVersioncountItem();            //总记录数
        PageBean<RdbVersionModel> pageData = new PageBean<>(currentPage, pageSize, countNums);
        pageData.setItems(allItems);
        return pageData;
    }

    @Override
    public List<RdbVersionModel> selectAll() throws Exception {
        return SqliteOPUtils.RdbVersionSelectAll();
    }

    @Override
    public boolean deleteRdbVersionModelById(Integer id) throws Exception {
        return SqliteOPUtils.deleteRdbVersionModelById(id);
    }

    @Override
    public RdbVersionModel findRdbVersionModelById(Integer id) throws Exception {
        return SqliteOPUtils.findRdbVersionModelById(id);
    }

    @Override
    public RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion, Integer rdbVersion) throws Exception {
        return SqliteOPUtils.findRdbVersionModelByRedisVersionAndRdbVersion(redisVersion,rdbVersion);
    }

    @Override
    public boolean insertRdbVersionModel(RdbVersionModel rdbVersionModel) throws Exception {
        return SqliteOPUtils.insertRdbVersionModel(rdbVersionModel);
    }

    @Override
    public boolean updateRdbVersionModelById(Integer id, String redisVersion, Integer rdbVersion) throws Exception {
        return SqliteOPUtils.updateRdbVersionModelById(id,redisVersion,rdbVersion);
    }
}
