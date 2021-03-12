package syncer.transmission.etcd.client;


import com.google.protobuf.ByteString;
import com.ibm.etcd.api.*;
import com.ibm.etcd.client.EtcdClient;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import com.ibm.etcd.client.lease.LeaseClient;
import com.ibm.etcd.client.lease.PersistentLease;
import com.ibm.etcd.client.lock.LockClient;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import syncer.common.config.EtcdServerConfig;
import syncer.transmission.constants.EtcdKeyCmd;
import syncer.transmission.etcd.IEtcdOpCenter;
import syncer.transmission.etcd.SystemClock;
import syncer.transmission.lock.EtcdLockCommandRunner;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author: Eq Zhan
 * @create: 2021-02-20
 **/
@Slf4j
public class JEtcdClient implements IEtcdOpCenter {
    private EtcdServerConfig config=new EtcdServerConfig();
    private KvClient kvClient;
    private LeaseClient leaseClient;
    private LockClient lockClient;
    private KvStoreClient kvStoreClient;
    private static SystemClock systemClock=new SystemClock(1);
    private Client client ;
    public JEtcdClient(KvStoreClient kvStoreClient, String url) {
        this.kvClient = kvStoreClient.getKvClient();
        this.leaseClient = kvStoreClient.getLeaseClient();
        this.lockClient = kvStoreClient.getLockClient();
        this.kvStoreClient=kvStoreClient;
        this.client=Client.builder().endpoints(url).build();
    }



    public JEtcdClient(KvStoreClient kvStoreClient, String url, String username, String password) {
        if (Objects.nonNull(username) && Objects.nonNull(password)) {
//            this.client = Client.builder().endpoints(config.getUrl()).user(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getUsername()))).password(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getPassword()))).build();
            this.kvStoreClient=EtcdClient.forEndpoints(config.getEtcdConfig().getUrl()).withCredentials(username,password).withPlainText().build();
        } else {
            this.kvStoreClient = EtcdClient.forEndpoints(url).withPlainText().build();
        }

        this.kvClient = kvStoreClient.getKvClient();
        this.leaseClient = kvStoreClient.getLeaseClient();
        this.lockClient = kvStoreClient.getLockClient();
        if(Objects.nonNull(username)&&Objects.nonNull(password)){
            this.client=Client.builder().endpoints(url).user(ByteSequence.from(ByteString.copyFromUtf8(username))).password(ByteSequence.from(ByteString.copyFromUtf8(password))).build();
        }else {
            this.client=Client.builder().endpoints(url).build();
        }
    }

    public JEtcdClient(KvStoreClient kvStoreClient) {
        this.kvClient = kvStoreClient.getKvClient();
        this.leaseClient = kvStoreClient.getLeaseClient();
        this.lockClient = kvStoreClient.getLockClient();
        this.kvStoreClient=kvStoreClient;
        if(Objects.nonNull(config.getEtcdConfig().getUsername())&&Objects.nonNull(config.getEtcdConfig().getPassword())){
            this.client=Client.builder().endpoints(config.getUrl()).user(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getUsername()))).password(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getPassword()))).build();
        }else {
            this.client=Client.builder().endpoints(config.getUrl()).build();
        }
    }


    public JEtcdClient() {
        if (Objects.nonNull(config.getEtcdConfig().getUsername()) && Objects.nonNull(config.getEtcdConfig().getPassword())) {
//            this.client = Client.builder().endpoints(config.getUrl()).user(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getUsername()))).password(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getPassword()))).build();
            this.kvStoreClient=EtcdClient.forEndpoints(config.getEtcdConfig().getUrl()).withCredentials(config.getEtcdConfig().getUsername(),config.getEtcdConfig().getPassword()).withPlainText().build();
        } else {
            this.kvStoreClient = EtcdClient.forEndpoints(config.getEtcdConfig().getUrl()).withPlainText().build();
        }

        this.kvClient = kvStoreClient.getKvClient();
        this.leaseClient = kvStoreClient.getLeaseClient();
        this.lockClient = kvStoreClient.getLockClient();
        if(Objects.nonNull(config.getEtcdConfig().getUsername())&&Objects.nonNull(config.getEtcdConfig().getPassword())){
            this.client=Client.builder().endpoints(config.getEtcdConfig().getUrl()).user(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getUsername()))).password(ByteSequence.from(ByteString.copyFromUtf8(config.getEtcdConfig().getPassword()))).build();
        }else {
            this.client=Client.builder().endpoints(config.getEtcdConfig().getUrl()).build();
        }
    }

    @Override
    public void put(String key, String value) {
        kvClient.put(ByteString.copyFromUtf8(key), ByteString.copyFromUtf8(value)).sync();
    }

    @Override
    public void put(String key, String value, long leaseId) {
        kvClient.put(ByteString.copyFromUtf8(key), ByteString.copyFromUtf8(value), leaseId).sync();
    }

    @Override
    public void putAndGrant(String key, String value, long ttl) {
        LeaseGrantResponse lease = leaseClient.grant(ttl).sync();
        put(key, value, lease.getID());
    }

    @Override
    public String get(String key) {
        RangeResponse rangeResponse = kvClient.get(ByteString.copyFromUtf8(key)).sync();
        List<KeyValue> keyValues = rangeResponse.getKvsList();
        if (CollectionUtils.isEmpty(keyValues)) {
            return null;
        }
        return keyValues.get(0).getValue().toStringUtf8();

    }

    @Override
    public List<KeyValue> getPrefix(String key) {
        RangeResponse rangeResponse = kvClient.get(ByteString.copyFromUtf8(key)).asPrefix().sync();
        return rangeResponse.getKvsList();
    }

    @Override
    public long deleteByKey(String key) {
        DeleteRangeResponse response=kvClient.delete(ByteString.copyFromUtf8(key)).sync();
        return response.getDeleted();
    }

    @Override
    public long deleteByKeyPrefix(String keyPrefix) {

        try {
            return kvClient.delete(DeleteRangeRequest.newBuilder().setPrevKv(true).setKey(ByteString.copyFromUtf8(EtcdKeyCmd.getTasksTaskIdPrefix())).build()).get().getDeleted();
        } catch (Exception e) {
            log.error("deleteByKeyPrefix fail keyName [{}]",keyPrefix);
        }
        return 0L;
    }

    @Override
    public KvClient.WatchIterator watch(String key) {
        return kvClient.watch(ByteString.copyFromUtf8(key)).start();
    }

    @Override
    public KvClient.WatchIterator watchPrefix(String key) {
        return kvClient.watch(ByteString.copyFromUtf8(key)).asPrefix().start();
    }


    @Override
    public long keepAlive(String key, String value, int frequencySecs, int minTtl) throws Exception {
        //minTtl秒租期，每frequencySecs秒续约一下
        PersistentLease lease = leaseClient.maintain().leaseId(systemClock.now()).keepAliveFreq(frequencySecs).minTtl(minTtl).start();
        long newId = lease.get(3L, SECONDS);
        put(key, value, newId);
        return newId;

    }

    @Override
    public long timeToLive(long leaseId) {
        try {
            return leaseClient.ttl(leaseId).get().getTTL();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public void lockCommandRunner(EtcdLockCommandRunner commandRunner) {
        try {
            //创建一个租约
            Lease lease=client.getLeaseClient();
            Lock lock=client.getLockClient();
            ByteSequence name = ByteSequence.from(commandRunner.lockName().getBytes());
            io.etcd.jetcd.lease.LeaseGrantResponse ttl = lease.grant(30).get();
//            log.info("创建租约...ID: {}, ttl: {}", ttl.getID(), ttl.getTTL());
            //自动续约-防止ttl超时线程未执行完
            lease.keepAlive(ttl.getID(), new StreamObserver() {
                @Override
                public void onNext(Object value) {
                    //执行续约之后的回调
//                    log.info("执行续约...ID: {}, ttl: {}", ttl.getID(), ttl.getTTL());
                }

                @Override
                public void onError(Throwable t) {
                    //异常，就会停止续约，比如调用revoke取消续约，租约不存在
//                    log.info("停止续约...{} 原因: {}", ttl.getID(), t.getMessage());
                }

                @Override
                public void onCompleted() {
                    log.info("onCompleted...");
                }
            });
//            log.info("尝试获取锁...");
            //尝试获取锁
            ByteSequence key = lock.lock(name, ttl.getID()).get().getKey();
            long start = System.currentTimeMillis();
//            log.info("获得锁...key: {}", new String(key.getBytes()));
            commandRunner.run();
//            log.info("执行时长... {} ms", System.currentTimeMillis() - start);
            //取消租约
            lease.revoke(ttl.getID());
//            log.info("取消租约...{}", ttl.getID());
            lock.unlock(key);
//            log.info("释放锁...");
        } catch (Exception e) {
            log.warn("etcd lock error {}",e.getMessage());
        }
    }

    @Override
    public KvClient getKvClient() {
        return this.kvClient;
    }

    @Override
    public void close() {
        try {
            if(Objects.nonNull(kvClient)){
                client.close();
            }
            if(Objects.nonNull(kvClient)){
                kvStoreClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void lockCommandRunner1(EtcdLockCommandRunner commandRunner) {
        try {

            LeaseGrantResponse ttl = leaseClient.grant(30).sync();
            PersistentLease lease = leaseClient.maintain().leaseId(ttl.getID()).keepAliveFreq(12).start(new StreamObserver<PersistentLease.LeaseState>() {
                @Override
                public void onNext(PersistentLease.LeaseState leaseState) {
                    log.info("执行续约...ID: {}, ttl: {}", ttl.getID(), ttl.getTTL());
                }
                @Override
                public void onError(Throwable e) {
                    log.info("停止续约...{} 原因: {}", ttl.getID(), e.getMessage());
                }

                @Override
                public void onCompleted() {
                    log.info("onCompleted...");
                }
            });

            String key=commandRunner.lockName();
            ByteString lockKey = lockClient.lock(ByteString.copyFromUtf8(key)).withLease(lease).sync().getKey();
            log.info("获得锁...key: {}", new String(lockKey.toStringUtf8()));


            commandRunner.run();

            //取消租约
            leaseClient.revoke(ttl.getID());
            log.info("取消租约...{}", ttl.getID());

            lockClient.unlock(lockKey).sync();
            log.info("释放锁...");

        }catch (Exception e){
            log.warn("etcd lock is fail,message {}",e.getMessage());
            e.printStackTrace();
        }

    }

    static String t(long start) {
        return String.format("%.3f ", (System.currentTimeMillis() - start) / 1000.0);
    }
    /**
     * @param endPoints 如https://127.0.0.1:2379 有多个时逗号分隔
     */
    public static JEtcdClient build(String endPoints) {
        return new JEtcdClient(EtcdClient.forEndpoints(endPoints).withPlainText().build(),endPoints);
    }

    public static JEtcdClient build(String endPoints,String username,String password) {
        return new JEtcdClient(EtcdClient.forEndpoints(endPoints).withPlainText().build(),endPoints,username,password);
    }


    public static JEtcdClient build() {
        return new JEtcdClient();
    }

}
