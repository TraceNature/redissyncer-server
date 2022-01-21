package syncer.transmission.mapper.etcd;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.entity.EtcdIDEntity;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.lock.EtcdLockCommandRunner;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * etcd 全局id
 * /tasks/id/{idKey} {"id":1}
 * @author: Eq Zhan
 * @create: 2021-03-09
 **/

@Builder
@Data
@Slf4j
public class EtcdID {
    private JEtcdClient client ;
    private String nodeId;
    final static String idLockName="idLock";
    public  int getID(String idKey) throws Exception {
        AtomicInteger res=new AtomicInteger(-1);
        client.lockCommandRunner(new EtcdLockCommandRunner() {
            @Override
            public void run() {
                try {
                    String value=client.get(EtcdKeyCmd.getIdKey(idKey));
                    if(Objects.nonNull(value)){
                        EtcdIDEntity etcdIDEntity= JSON.parseObject(value,EtcdIDEntity.class);
                        int id=etcdIDEntity.getId().incrementAndGet();
                        client.put(EtcdKeyCmd.getIdKey(idKey),JSON.toJSONString(etcdIDEntity));
                        res.set(id);
                    }else {
                        EtcdIDEntity etcdIDEntity=EtcdIDEntity.builder().id(new AtomicInteger(1)).build();
                        client.put(EtcdKeyCmd.getIdKey(idKey),JSON.toJSONString(etcdIDEntity));
                        res.set(1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String lockName() {
                return EtcdKeyCmd.getLockName(idLockName, idKey);
            }

            @Override
            public int grant() {
                return 30;
            }
        });

        if(res.get()==-1){
            throw new Exception("全局id生成失败");
        }
        return res.get();

    }
}
