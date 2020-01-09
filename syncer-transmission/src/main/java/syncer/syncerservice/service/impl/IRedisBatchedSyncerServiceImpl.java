package syncer.syncerservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import syncer.syncerplusredis.constant.KeyValueEnum;
import syncer.syncerplusredis.constant.RedisBranchTypeEnum;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisInfo;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisSyncDataDto;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerservice.service.IRedisSyncerService;
import syncer.syncerservice.sync.RedisCommandBackUpTransmissionTask;
import syncer.syncerservice.sync.RedisDataTransmissionTask;
import syncer.syncerservice.util.RedisUrlCheckUtils;

import java.net.URISyntaxException;
import java.util.Set;

@Service("redisBatchedSyncerService")
@Slf4j
public class IRedisBatchedSyncerServiceImpl implements IRedisSyncerService {
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void batchedSync(RedisClusterDto clusterDto, String taskId, boolean afresh) throws TaskMsgException {

        Set<String> targetRedisUris = clusterDto.getTargetUris();

        if(clusterDto.getFileType().equals(FileType.SYNC)){
            Set<String> sourceRedisUris = clusterDto.getSourceUris();
            for (String sourceUri : sourceRedisUris) {
                RedisUrlCheckUtils.checkRedisUrl(sourceUri, " sourceUri: " + sourceUri);
            }

            /**
             * 存在dbMap时检查是否超出数据库大小
             */
//            try {
//                RedisUrlCheckUtils.doCheckDbNum(sourceRedisUris, clusterDto.getDbMapper(), KeyValueEnum.KEY);
//            } catch (URISyntaxException e) {
//                throw new TaskMsgException(e.getMessage());
//            }
        }else{
            clusterDto.setSourceUris(clusterDto.getFileUris());
        }

        for (String targetUri : targetRedisUris) {

            RedisUrlCheckUtils.checkRedisUrl(targetUri, "targetUri : " + targetUri);
        }



//        try {
//            RedisUrlCheckUtils.doCheckDbNum(targetRedisUris, clusterDto.getDbMapper(), KeyValueEnum.VALUE);
//        } catch (URISyntaxException e) {
//            throw new TaskMsgException(e.getMessage());
//        }




        if (clusterDto.getTargetUris().size() == 1) {
            //单机 间或者往京东云集群迁移
            batchedSyncToSingle(clusterDto,taskId,afresh);

        } else if (null!=clusterDto.getSourceUris()&&clusterDto.getSourceUris().size() == 1 && clusterDto.getTargetUris().size() > 1) {



            //单机往cluster迁移
            batchedSyncSingleToCluster(clusterDto,taskId,afresh);
        } else {
            //cluster
            batchedSyncToCluster(clusterDto,taskId,afresh);
        }

    }


    /**
     * cluster往cluster迁移
     * @param clusterDto
     * @param taskId
     * @param afresh
     */
    private void batchedSyncToCluster(RedisClusterDto clusterDto, String taskId, boolean afresh) throws TaskMsgException {
        System.out.println("-----------------batchedSyncToCluster");

        Set<String> targetRedisUris = clusterDto.getTargetUris();
        Set<String> sourceRedisUris =null;
        if(clusterDto.getFileType().equals(FileType.SYNC)){
            sourceRedisUris = clusterDto.getSourceUris();
            for (String sourceUri : sourceRedisUris) {
                RedisUrlCheckUtils.checkRedisUrl(sourceUri, " sourceUri:" + sourceUri);
            }

        }else {
            sourceRedisUris= clusterDto.getFileUris();
        }


        for (String targetUri : targetRedisUris) {
            RedisUrlCheckUtils.checkRedisUrl(targetUri, "targetUri" + targetUri);
        }

        int i=0;
        for (String sourceUrl : sourceRedisUris) {
            RedisSyncDataDto syncDataDto = new RedisSyncDataDto();
            BeanUtils.copyProperties(clusterDto, syncDataDto);
            syncDataDto.setTargetHost(clusterDto.getTargetRedisAddress());
            syncDataDto.setTargetPassword(clusterDto.getTargetPassword());
            syncDataDto.setSourceUri(sourceUrl);

            threadPoolTaskExecutor.execute(new RedisDataTransmissionTask(syncDataDto, (RedisInfo) clusterDto.getTargetUriData().toArray()[i++],taskId,clusterDto.getBatchSize(),afresh, RedisBranchTypeEnum.CLUSTER));

            //threadPoolTaskExecutor.execute(new SingleDataPipelineRestoreTask(clusterDto, (RedisInfo) clusterDto.getTargetUriData().toArray()[i],sourceUrl,taskId,afresh));

        }
        log.info("--------集群到集群-------");
    }




    /**
     * 单机往cluster迁移
     * @param clusterDto
     * @param taskId
     * @param afresh
     */
    private void batchedSyncSingleToCluster(RedisClusterDto clusterDto, String taskId, boolean afresh) throws TaskMsgException {


        Set<String> targetRedisUris = clusterDto.getTargetUris();
        Set<String> sourceRedisUris =null;

        if(clusterDto.getFileType().equals(FileType.SYNC)){
            sourceRedisUris=clusterDto.getSourceUris();
            for (String sourceUri : sourceRedisUris) {
                RedisUrlCheckUtils.checkRedisUrl(sourceUri, " sourceUri: " + sourceUri);
            }
        }else {
            sourceRedisUris=clusterDto.getFileUris();
        }


        for (String targetUri : targetRedisUris) {
            RedisUrlCheckUtils.checkRedisUrl(targetUri, " sourceUri: " + targetUri);
        }

        RedisSyncDataDto syncDataDto = new RedisSyncDataDto();
        BeanUtils.copyProperties(clusterDto, syncDataDto);


        for (String data:sourceRedisUris){
            syncDataDto.setSourceUri(data);
        }
        syncDataDto.setTargetPassword(clusterDto.getTargetPassword());
        syncDataDto.setTargetHost(clusterDto.getTargetRedisAddress());

        //进行数据同步
        threadPoolTaskExecutor.execute(new RedisDataTransmissionTask(syncDataDto, (RedisInfo) clusterDto.getTargetUriData().toArray()[0],taskId,clusterDto.getBatchSize(),afresh, RedisBranchTypeEnum.CLUSTER));
        log.info("--------单机到集群-------");


    }



    /**
     * 单机往cluster迁移
     * @param clusterDto
     * @param taskId
     * @throws TaskMsgException
     */

    @Override
    public void filebatchedSync(RedisClusterDto clusterDto, String taskId) throws TaskMsgException {

    }

    //备份数据
    @Override
    public void fileCommandBackUpSync(RedisClusterDto clusterDto, String taskId) throws TaskMsgException {
        /**
         * 获取所有地址并处理新建线程进行同步
         */
        Set<String> sourceRedisUris =clusterDto.getSourceUris();
        String fileAddress=clusterDto.getFileAddress();


        RedisUrlCheckUtils.checkRedisUrl((String) sourceRedisUris.toArray()[0], " sourceUri: " + sourceRedisUris.toArray()[0]);



            //进行数据同步
        threadPoolTaskExecutor.execute(new RedisCommandBackUpTransmissionTask(taskId,fileAddress, (String) sourceRedisUris.toArray()[0]));


    }


    /**
     * psync单机同步
     * @param clusterDto
     * @param taskId
     * @param afresh
     */

    private void batchedSyncToSingle(RedisClusterDto clusterDto, String taskId,boolean afresh) {
        /**
         * 获取所有地址并处理新建线程进行同步
         */
        Set<String> sourceRedisUris =null;
        Set<String> targetRedisUris = clusterDto.getTargetUris();

        if(clusterDto.getFileType().equals(FileType.SYNC)){
            sourceRedisUris=clusterDto.getSourceUris();
        }else {
            sourceRedisUris=clusterDto.getFileUris();
        }
        int i = 0;


        for (String source : sourceRedisUris
        ) {
            RedisSyncDataDto syncDataDto = new RedisSyncDataDto();
            BeanUtils.copyProperties(clusterDto, syncDataDto);

            syncDataDto.setSourceUri(source);


            try {
                String targetUri=null;
                for(String data:targetRedisUris){
                    targetUri=data;
                    break;
                }
                RedisURI turi = new RedisURI(targetUri);
                Configuration sourceCon = Configuration.valueOf(turi);
                syncDataDto.setTargetHost(turi.getHost());
                syncDataDto.setTargetPort(turi.getPort());
                syncDataDto.setTargetPassword(sourceCon.getAuthPassword());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            syncDataDto.setTargetUri(String.valueOf(targetRedisUris.toArray()[0]));
            //进行数据同步
            threadPoolTaskExecutor.execute(new RedisDataTransmissionTask(syncDataDto, (RedisInfo) syncDataDto.getTargetUriData().toArray()[0],taskId,clusterDto.getBatchSize(),afresh, RedisBranchTypeEnum.SINGLE));

        }
    }

}
