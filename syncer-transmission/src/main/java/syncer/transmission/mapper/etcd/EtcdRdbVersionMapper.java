package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ibm.etcd.api.KeyValue;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.mapper.RdbVersionMapper;
import syncer.transmission.model.RdbVersionModel;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Etcd rdb版本映射
 * {"2.6": "6"}
 * /tasks/rdbversion/{redisVersion}/{rdbVersion}  {"id":1,"redis_version": "2.6","rdb_version": 6 }
 * /tasks/rdbversion/{rdbVersion}/{redisVersion}
 *
 * @author: Eq Zhan
 * @create: 2021-03-08
 **/
@Builder
@Data
@Slf4j
public class EtcdRdbVersionMapper implements RdbVersionMapper {
    private JEtcdClient client;
    private String nodeId;
    private EtcdID etcdID;
    private static final String lockName = "changeRdbVersion";

    /**
     * key prefix /tasks/rdbVersion/
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<RdbVersionModel> findAllRdbVersion() throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getRdbVersionPrefix());
        List<RdbVersionModel> rdbVersionModelList = Lists.newArrayList();
        if (Objects.isNull(keyValueList)) {
            return rdbVersionModelList;
        }

        keyValueList.forEach(keyValue -> {
            RdbVersionModel rdbVersionModel = JSON.parseObject(keyValue.getValue().toStringUtf8(), RdbVersionModel.class);
            if (Objects.nonNull(rdbVersionModel)) {
                rdbVersionModelList.add(rdbVersionModel);
            }
        });
        return rdbVersionModelList;
    }

    /**
     * Etcd 本接口弃用
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public RdbVersionModel findRdbVersionModelById(Integer id) throws Exception {

        return null;
    }


    /**
     * key /tasks/rdbVersion/{redisVersion}
     *
     * @param redisVersion
     * @return
     * @throws Exception
     */
    @Override
    public RdbVersionModel findRdbVersionModelByRedisVersion(String redisVersion) throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getRdbVersionByRedisVersionPrefix(redisVersion));
        if (Objects.nonNull(keyValueList) && keyValueList.size() > 0) {
            KeyValue keyValue = keyValueList.get(0);
            RdbVersionModel rdbVersionModel = JSON.parseObject(keyValue.getValue().toStringUtf8(), RdbVersionModel.class);
            return rdbVersionModel;
        }
        return null;
    }

    /**
     * @param redisVersion
     * @param rdbVersion
     * @return
     * @throws Exception
     */
    @Override
    public RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion, Integer rdbVersion) throws Exception {
        String value = client.get(EtcdKeyCmd.getRdbVersionByRedisVersionAndRdbVerison(redisVersion, rdbVersion));
        if (Objects.isNull(value)) {
            return null;
        }
        RdbVersionModel rdbVersionModel = JSON.parseObject(value, RdbVersionModel.class);
        return rdbVersionModel;
    }

    /**
     * Etcd版本抛弃
     * /tasks/
     *
     * @param rdbVersion
     * @return
     * @throws Exception
     */
    @Override
    public List<RdbVersionModel> findTaskByRdbVersion(Integer rdbVersion) throws Exception {
        return null;
    }


    @Override
    public boolean insertRdbVersionModel(RdbVersionModel rdbVersionModel) throws Exception {
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    //List<RdbVersionModel>rdbVersionModelList=findAllRdbVersion().stream().sorted((r1,r2)->r1.getId().compareTo(r2.getId())).collect(Collectors.toList());
                    int sortNum=etcdID.getID(lockName);
                    rdbVersionModel.setId(sortNum);
                    client.put(EtcdKeyCmd.getRdbVersionByRedisVersionAndRdbVerison(rdbVersionModel.getRedis_version(), rdbVersionModel.getRdb_version()), JSON.toJSONString(rdbVersionModel));
                } catch (Exception e) {
                    log.error("insert rdbVersion error , reason [{}]", e.getMessage());
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(lockName);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public int countItem() throws Exception {
        List<KeyValue> keyValueList = client.getPrefix(EtcdKeyCmd.getRdbVersionPrefix());
        return keyValueList.size();
    }

    @Override
    public boolean updateRdbVersionModelById(Integer id, String redisVersion, Integer rdbVersion) throws Exception {

        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    List<RdbVersionModel> versionModelList = findAllRdbVersion().stream().filter(rdbVersionModel -> {
                        return rdbVersionModel.getId().equals(id);
                    }).collect(Collectors.toList());

                    if (Objects.nonNull(versionModelList) && versionModelList.size() > 0) {
                        RdbVersionModel rdbVersionModel = versionModelList.get(0);
                        rdbVersionModel.setRdb_version(rdbVersion);
                        rdbVersionModel.setRedis_version(redisVersion);
                        client.put(EtcdKeyCmd.getRdbVersionByRedisVersionAndRdbVerison(redisVersion, rdbVersion), JSON.toJSONString(rdbVersionModel));
                    }

                } catch (Exception e) {
                    log.error("update rdbVersion error reason[{}]", e.getMessage());
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(lockName);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return true;
    }

    @Override
    public int insertRdbVersionModelList(List<RdbVersionModel> rdbVersionModelList) {
        AtomicInteger num=new AtomicInteger(0);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    int sortNum=0;

                    for (int i = 0; i < rdbVersionModelList.size(); i++) {
                        sortNum=etcdID.getID(lockName);
                        RdbVersionModel rdbVersionModel=rdbVersionModelList.get(i);
                        client.put(EtcdKeyCmd.getRdbVersionByRedisVersionAndRdbVerison(rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version()),JSON.toJSONString(RdbVersionModel.builder().id(sortNum).redis_version(rdbVersionModel.getRedis_version()).rdb_version(rdbVersionModel.getRdb_version()).build()));
                        num.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("insertRdbVersionModelList error , reason [{}]", e.getMessage());
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(lockName);
            }

            @Override
            public int grant() {
                return 30;
            }
        });
        return num.get();
    }

    @Override
    public boolean deleteRdbVersionModelById(Integer id) throws Exception {
        AtomicBoolean status=new AtomicBoolean(false);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    List<RdbVersionModel>oldRdbVersionModelList=findAllRdbVersion().stream().sorted((r1,r2)->r1.getId().compareTo(r2.getId())).filter(rdbVersionModel -> {
                        return rdbVersionModel.getId().equals(id);
                    }).collect(Collectors.toList());
                    if(Objects.isNull(oldRdbVersionModelList)){
                        status.set(false);
                        return;
                    }
                    for (int i = 0; i < oldRdbVersionModelList.size(); i++) {
                        RdbVersionModel rdbVersionModel=oldRdbVersionModelList.get(i);
                        client.deleteByKey(EtcdKeyCmd.getRdbVersionByRedisVersionAndRdbVerison(rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version()));
                    }
                    status.set(true);
                } catch (Exception e) {
                    log.error("deleteRdbVersionModelById  error , reason [{}]", e.getMessage());
                }
            }
            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(lockName);
            }
            @Override
            public int grant() {
                return 30;
            }
        });
        return status.get();
    }

    @Override
    public int deleteRdbVersionModelByRedisVersion(String redisVersion) throws Exception {
        AtomicInteger num=new AtomicInteger(0);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    long res=client.deleteByKeyPrefix(EtcdKeyCmd.getRdbVersionByRedisVersionPrefix(redisVersion));
                    num.set(Math.toIntExact(res));
                } catch (Exception e) {
                    log.error("deleteRdbVersionModelByRedisVersion  error , reason [{}]", e.getMessage());
                }
            }
            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(lockName);
            }
            @Override
            public int grant() {
                return 30;
            }
        });

        return num.get();
    }

    @Override
    public int deleteAllRdbVersionModel() {
        AtomicInteger num=new AtomicInteger(0);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    long res=client.deleteByKeyPrefix(EtcdKeyCmd.getRdbVersionPrefix());
                    num.set(Math.toIntExact(res));
                } catch (Exception e) {
                    log.error("deleteAllRdbVersionModel  error , reason [{}]", e.getMessage());
                }
            }
            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(lockName);
            }
            @Override
            public int grant() {
                return 30;
            }
        });

        return num.get();
    }

    public static void main(String[] args) {
        List<RdbVersionModel>rdbVersionModelList=Lists.newArrayList();
        rdbVersionModelList.add(RdbVersionModel.builder().id(1).build());
        rdbVersionModelList.add(RdbVersionModel.builder().id(3).build());
        rdbVersionModelList.add(RdbVersionModel.builder().id(5).build());
        rdbVersionModelList.add(RdbVersionModel.builder().id(2).build());
        List<RdbVersionModel>rdbVersionModelList1=rdbVersionModelList.stream().sorted((r1,r2)->r1.getId().compareTo(r2.getId())).collect(Collectors.toList());
        rdbVersionModelList1.forEach(s->{
            System.out.println(s.getId());
        });
    }
}
