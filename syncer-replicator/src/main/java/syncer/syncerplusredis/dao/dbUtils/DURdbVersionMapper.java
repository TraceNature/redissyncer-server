package syncer.syncerplusredis.dao.dbUtils;

import syncer.syncerpluscommon.util.Type2TypeUtils;
import syncer.syncerpluscommon.util.db.SqlUtils;
import syncer.syncerplusredis.dao.RdbVersionMapper;
import syncer.syncerplusredis.model.RdbVersionModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class DURdbVersionMapper implements RdbVersionMapper {
    @Override
    public List<RdbVersionModel> selectAll() throws Exception {
        String sql="SELECT * FROM t_rdb_version";
        List<RdbVersionModel>data= SqlUtils.getForList(RdbVersionModel.class,sql);
        return data;
    }

    @Override
    public RdbVersionModel findRdbVersionModelById(Integer id) throws Exception {
        String sql="SELECT * FROM t_rdb_version WHERE id =?";
        Object[]objects={id};
        RdbVersionModel data= SqlUtils.get(RdbVersionModel.class,sql,objects);
        return data;
    }

    @Override
    public RdbVersionModel findRdbVersionModelByRedisVersion(String redisVersion) throws Exception {
        String sql="SELECT * FROM t_rdb_version WHERE redis_version =? limit 1";
        Object[]objects={redisVersion};
        RdbVersionModel data= SqlUtils.get(RdbVersionModel.class,sql,objects);
        return data;
    }

    @Override
    public RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion, Integer rdbVersion) throws Exception {
        String sql="SELECT * FROM t_rdb_version WHERE redis_version =?  and rdb_version =? limit 1";
        Object[]objects={redisVersion,rdbVersion};
        RdbVersionModel data= SqlUtils.get(RdbVersionModel.class,sql,objects);
        return data;
    }

    @Override
    public List<RdbVersionModel> findTaskByRdbVersion(Integer rdbVersion) throws Exception {
        String sql="SELECT * FROM t_rdb_version WHERE rdb_version =?";
        Object[]objects={rdbVersion};
        List<RdbVersionModel>data= SqlUtils.getForList(RdbVersionModel.class,sql,objects);
        return data;
    }

    @Override
    public boolean insertRdbVersionModel(RdbVersionModel rdbVersionModel) throws Exception {
        String sql="INSERT INTO t_rdb_version(redis_version,rdb_version) VALUES(?,?)";
        Object[]objects={rdbVersionModel.getRedis_version(),
                rdbVersionModel.getRdb_version()};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public int countItem() throws Exception {
        return 0;
    }

    @Override
    public boolean updateRdbVersionModelById(Integer id, String redisVersion, Integer rdbVersion) throws Exception {
        String sql="UPDATE  t_rdb_version  set redis_version=?, rdb_version =? WHERE id =?";
        Object[]objects={redisVersion,
                rdbVersion,
                id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public int insertRdbVersionModelList(List<RdbVersionModel> rdbVersionModelList) {
        String sql="insert into t_rdb_version(redis_version,rdb_version) values(?,?)";
        List<Object[]>data=new ArrayList<>();
        for (RdbVersionModel rdb:rdbVersionModelList
             ) {
            Object[]objects={rdb.getRedis_version(),
                    rdb.getRdb_version()};
            data.add(objects);
        }
        try {
            return SqlUtils.updateBatch(sql,data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean deleteRdbVersionModelById(Integer id) throws Exception {
        String sql="DELETE FROM t_rdb_version WHERE id=?";
        Object[]objects={id};
        return Type2TypeUtils.int2boolean(SqlUtils.update(sql,objects));
    }

    @Override
    public int deleteRdbVersionModelByRedisVersion(String redisVersion) throws Exception {
        String sql="DELETE FROM t_rdb_version WHERE redis_version=?";
        Object[]objects={redisVersion};
        return SqlUtils.update(sql,objects);
    }

    @Override
    public int deleteAllRdbVersionModel() {
        String sql="DELETE FROM t_rdb_version";
        return SqlUtils.update(sql);
    }
}
