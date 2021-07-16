package syncer.webapp.start;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.common.config.EtcdServerConfig;
import syncer.common.util.TemplateUtils;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;
import syncer.transmission.mapper.UserMapper;
import syncer.transmission.mapper.etcd.EtcdID;
import syncer.transmission.model.RdbVersionModel;
import syncer.transmission.model.UserModel;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.Objects;

@Slf4j
public class NodeStartInitService {
    private EtcdServerConfig config= new EtcdServerConfig();
    private static final String lockName = "changeRdbVersion";
    /**
     * 初始化资源
     *
     * /tasks/user/{username}  {"id":"","username":"","name":"","password":"","salt":""}
     *
     */
    public void initResource(){
        JEtcdClient client= JEtcdClient.build();
        try {
            EtcdID etcdID= EtcdID.builder().client(client).nodeId(config.getNodeId()).build();
            client.lockCommandRunner(new EtcdLockCommandRunner() {
                @Override
                public void run() {
                    String userKey=EtcdKeyCmd.getUserByUserName("admin");
                    String user=client.get(userKey);
                    if(Objects.isNull(user)){
                        log.info("user init success");
                        client.put(userKey,JSON.toJSONString(UserModel.builder().username("admin").password("123456").salt(TemplateUtils.uuid()).build()));
                    }
                    SingleTaskDataManagerUtils.getRDB_VERSION_MAP().entrySet().stream().forEach(version->{
                        try {
                            String vkey=EtcdKeyCmd.getRdbVersionByRedisVersionAndRdbVerison(version.getKey(),version.getValue());
                            String result = client.get(vkey);
                            int sortNum=etcdID.getID(lockName);
                            if(Objects.isNull(result)){
                                client.put(vkey, JSON.toJSONString(RdbVersionModel.builder()
                                        .rdb_version(version.getValue())
                                        .redis_version(version.getKey())
                                        .id(sortNum)
                                        .build()));
                            }
                        }catch (Exception e){
                            log.error("insert etcd rdb version fail {}",e.getMessage());
                        }
                    });

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
        }catch (Exception e){
            log.error("initResource fail {}",e.getMessage());
            throw e;

        }finally {
            client.close();
        }

    }

    public void initStatusResource() {
        JEtcdClient client = JEtcdClient.build();
    }
}
