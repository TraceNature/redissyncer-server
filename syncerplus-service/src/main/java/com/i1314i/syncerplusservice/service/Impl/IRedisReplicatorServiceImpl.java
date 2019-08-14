package com.i1314i.syncerplusservice.service.Impl;

import com.i1314i.syncerpluscommon.util.common.TemplateUtils;
import com.i1314i.syncerplusservice.constant.KeyValueEnum;
import com.i1314i.syncerplusservice.constant.RedisVersion;
import com.i1314i.syncerplusservice.constant.TaskMsgConstant;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisJDClousterClusterDto;
import com.i1314i.syncerplusservice.entity.dto.RedisSyncDataDto;
import com.i1314i.syncerplusservice.service.IRedisReplicatorService;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.i1314i.syncerplusservice.task.*;
import com.i1314i.syncerplusservice.task.clusterTask.ClusterRdbSameVersionJDCloudRestoreTask;
import com.i1314i.syncerplusservice.task.clusterTask.pipelineVersion.ClusterRdbSameVersionJDCloudPipelineRestoreTask;
import com.i1314i.syncerplusservice.task.singleTask.defaultVersion.SyncTask;
import com.i1314i.syncerplusservice.task.singleTask.diffVersion.defaultVersion.SyncDiffTask;
import com.i1314i.syncerplusservice.task.singleTask.diffVersion.pipeVersion.SyncPipeDiffTask;
import com.i1314i.syncerplusservice.task.singleTask.lowerVersion.defaultVersion.SyncLowerTask;
import com.i1314i.syncerplusservice.task.singleTask.lowerVersion.pipeVersion.SyncPipeSameLowerTask;
import com.i1314i.syncerplusservice.task.singleTask.sameVersion.defaultVersion.SyncSameTask;
import com.i1314i.syncerplusservice.task.singleTask.sameVersion.pipeVersion.SyncPipeSameTask;
import com.i1314i.syncerplusservice.util.RedisUrlUtils;
import com.i1314i.syncerplusservice.util.TaskMonitorUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@Service("redisReplicatorService")
@Slf4j
public class IRedisReplicatorServiceImpl implements IRedisReplicatorService {
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * AOF备份
     *
     * @param redisPath
     * @param aofPath
     * @throws Exception
     */
    @Override
    public void backupAof(String redisPath, String aofPath) {
        /**
        File file = new File(aofPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.info("create new file fail because:{%s}", e.getMessage());
            }
        }

        try {
            final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            final RawByteListener rawByteListener = new RawByteListener() {
                @Override
                public void handle(byte... rawBytes) {
                    try {
                        out.write(rawBytes);
                    } catch (IOException ignore) {
                    }
                }
            };

            Replicator replicator = new RedisReplicator(redisPath);
            replicator.addRdbListener(new RdbListener() {
                @Override
                public void preFullSync(Replicator replicator) {
                }

                @Override
                public void handle(Replicator replicator, KeyValuePair<?> kv) {
                }

                @Override
                public void postFullSync(Replicator replicator, long checksum) {
                    replicator.addRawByteListener(rawByteListener);
                }
            });

            final AtomicInteger acc = new AtomicInteger(0);
            replicator.addCommandListener(new CommandListener() {
                @Override
                public void handle(Replicator replicator, Command command) {
                    if (acc.incrementAndGet() == 1000) {
                        try {
                            out.close();
                            replicator.close();
                        } catch (Exception e) {

                        }
                    }
                }
            });
            replicator.open();
        } catch (Exception e) {
            log.info("[backupAof run error and reason is {%s}]", e.getMessage());
        }

         **/
    }


    /**
     * 远程备份RDB文件
     *
     * @param redisPath redis://127.0.0.1:6379?authPassword=yourPassword
     * @param path c://test.RDB
     * @throws Exception
     */
    @Override
    public void backUPRdb(String redisPath, String path) {
        Future<Integer> result = threadPoolTaskExecutor.submit(new BackUPRdbTask(redisPath,path));
        try {
            System.out.println("status:----"+result.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * 单机redis数据迁移
     * @param sourceUri  redis://127.0.0.1:6379?authPassword=yourPassword
     * @param targetUri redis://127.0.0.1:6380?authPassword=yourPassword
     */
    @Override
    public void sync(String sourceUri, String targetUri) throws TaskMsgException {
        checkRedisUrl(sourceUri,"sourceUri");
        checkRedisUrl(targetUri,"targetUri");
        String threadName= TemplateUtils.uuid();
        if(TaskMonitorUtils.containsKeyAliveMap(threadName)){
            throw new TaskMsgException(TaskMsgConstant.Task_MSG_PARSE_ERROR_CODE);
        }
        threadPoolTaskExecutor.execute(new SyncTask(sourceUri,targetUri));
    }

    /**
     * 单机redis数据迁移
     * @param sourceUri redis://127.0.0.1:6379?authPassword=yourPassword
     * @param targetUri redis://127.0.0.1:6380?authPassword=yourPassword
     * @param threadName 任务名称
     */
    @Override
    public void sync(String sourceUri, String targetUri, String threadName) throws TaskMsgException {

        checkRedisUrl(sourceUri,"sourceUri");
        checkRedisUrl(targetUri,"targetUri");
        if(TaskMonitorUtils.containsKeyAliveMap(threadName)){
            throw new TaskMsgException(TaskMsgConstant.Task_MSG_PARSE_ERROR_CODE);
        }
        threadPoolTaskExecutor.execute(new SyncTask(sourceUri,targetUri,threadName));
    }

    @Override
    public void sync(RedisSyncDataDto syncDataDto) throws TaskMsgException {
        checkRedisUrl(syncDataDto.getSourceUri(),"sourceUri");
        checkRedisUrl(syncDataDto.getTargetUri(),"targetUri");
        RedisVersion redisVersion=null;
        try {
             redisVersion=RedisUrlUtils.selectSyncerVersion(syncDataDto.getSourceUri(),syncDataDto.getTargetUri());
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
        if(TaskMonitorUtils.containsKeyAliveMap(syncDataDto.getThreadName())){
            throw new TaskMsgException(TaskMsgConstant.Task_MSG_PARSE_ERROR_CODE);
        }

        if(redisVersion.equals(RedisVersion.SAME)){

            if(syncDataDto.getPipeline()!=null&&syncDataDto.getPipeline().toLowerCase().equals("on")){
                threadPoolTaskExecutor.execute(new SyncPipeSameTask(syncDataDto));
                log.info("同步同版本（>3.0）版本数据...(开启管道)");
//                log.info("同步不同版本（）版本数据...");
            }else {
                threadPoolTaskExecutor.execute(new SyncSameTask(syncDataDto));
                log.info("同步同版本（>3.0）版本数据...(非管道)");
            }


        }else if(redisVersion.equals(RedisVersion.LOWER)){
            threadPoolTaskExecutor.execute(new SyncLowerTask(syncDataDto));
            log.info("同步同版本（<3.0）版本数据...");
        }else if(redisVersion.equals(RedisVersion.OTHER)){
            if(syncDataDto.getPipeline()!=null&&syncDataDto.getPipeline().toLowerCase().equals("on")){
                threadPoolTaskExecutor.execute(new SyncPipeDiffTask(syncDataDto));
                log.info("同步不同版本（）版本数据...(开启管道)");
//                log.info("同步不同版本（）版本数据...");
           }else {
                threadPoolTaskExecutor.execute(new SyncDiffTask(syncDataDto));
                log.info("同步不同版本（）版本数据...(非管道)");
            }

        }else {
            threadPoolTaskExecutor.execute(new SyncTask(syncDataDto));
        }


    }


    /**
     * 新版接口
     * 若集群则A
     * 若单机则B
     * @param clusterDto
     * @throws TaskMsgException
     */
    @Override
    public void sync(RedisClusterDto clusterDto) throws TaskMsgException {
        Set<String> sourceRedisUris=clusterDto.getSourceUris();
        Set<String>targetRedisUris=clusterDto.getTargetUris();

        for (String sourceUri:sourceRedisUris){
            checkRedisUrl(sourceUri,"sourceUri: "+sourceUri);
        }

        for (String targetUri:targetRedisUris){
            checkRedisUrl(targetUri," sourceUri: "+targetUri);
        }


        try {
            RedisUrlUtils.doCheckDbNum(sourceRedisUris,clusterDto.getDbNum(), KeyValueEnum.KEY);
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }

        try {
            RedisUrlUtils.doCheckDbNum(targetRedisUris,clusterDto.getDbNum(), KeyValueEnum.VALUE);
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
        if(clusterDto.getTargetUris().size()==1){
            //单机 间或者往京东云集群迁移
            syncToSingle(clusterDto);
        }else if(clusterDto.getSourceUris().size()==1&&clusterDto.getTargetUris().size()>1) {

            for (String sourceUri:sourceRedisUris){
                checkRedisUrl(sourceUri,"sourceUri: "+sourceUri);
            }

            for (String targetUri:targetRedisUris){
                checkRedisUrl(targetUri,"sourceUri: "+targetUri);
            }

            //单机往cluster迁移
            syncSingleToCluster(clusterDto);
        }else {
            //cluster
            syncToCluster(clusterDto);
        }




    }

    /**
     * 目标为单机节点时
     * @param clusterDto
     */

    void syncToSingle(RedisClusterDto clusterDto) throws TaskMsgException {
        //获取所有的源地址（可能为集群可能为单个处理后是set集合）
        Set<String> sourceRedisUris = clusterDto.getSourceUris();
        //获得目标的地址 ---------疑问 获得目标地址时是一个我是直接获取targetRedisAddress ？
        // 还是需要遍历？ set集合时是否进行字符处理过？
        Set<String> targetRedisUris = clusterDto.getTargetUris();
        int i=0;
        for (String source:sourceRedisUris
             ) {
            RedisSyncDataDto syncDataDto = new RedisSyncDataDto();
            BeanUtils.copyProperties(clusterDto, syncDataDto);
            //set进去数据 copy完后线程池的信息都有 ----------疑问密码如何赋值？？？？？
            syncDataDto.setSourceUri(source);
            syncDataDto.setTargetUri(String.valueOf(targetRedisUris.toArray()[0]));
            //进行数据同步
            try {
                if(i==0){
                    syncTask(syncDataDto,source,String.valueOf(targetRedisUris.toArray()[0]),true);
                }else {
                    syncTask(syncDataDto,source,String.valueOf(targetRedisUris.toArray()[0]),false);
                }
                i++;
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 目标为集群节点时
     * @param clusterDto
     */
    void syncToCluster(RedisClusterDto clusterDto) throws TaskMsgException {
        Set<String> sourceRedisUris=clusterDto.getSourceUris();
        Set<String>targetRedisUris=clusterDto.getTargetUris();

        for (String sourceUri:sourceRedisUris){
            checkRedisUrl(sourceUri,"sourceUri: "+sourceUri);
        }

        for (String targetUri:targetRedisUris){
            checkRedisUrl(targetUri,"sourceUri: "+targetUri);
        }



        if(clusterDto.getPipeline()!=null&&clusterDto.getPipeline().toLowerCase().equals("on")){
            for (String sourceUrl:sourceRedisUris){
                threadPoolTaskExecutor.submit(new ClusterRdbSameVersionJDCloudPipelineRestoreTask(clusterDto, sourceUrl));

            }
            log.info("cluster版本（>3.0）版本数据...(开启管道)");
//                log.info("同步不同版本（）版本数据...");
        }else {
            for (String sourceUrl:sourceRedisUris){
                threadPoolTaskExecutor.submit(new ClusterRdbSameVersionJDCloudRestoreTask(clusterDto, sourceUrl));

            }

            log.info("cluster版本（>3.0）版本数据...(非管道)");
        }

        log.info("--------集群到集群-------");

    }

    /**
     * 单机到集群
     * @param clusterDto
     */
    void syncSingleToCluster(RedisClusterDto clusterDto) throws TaskMsgException {
        Set<String> sourceRedisUris=clusterDto.getSourceUris();
        Set<String>targetRedisUris=clusterDto.getTargetUris();

        for (String sourceUri:sourceRedisUris){
            checkRedisUrl(sourceUri,"sourceUri: "+sourceUri);
        }

        for (String targetUri:targetRedisUris){
            checkRedisUrl(targetUri,"sourceUri: "+targetUri);
        }

        threadPoolTaskExecutor.submit(new ClusterRdbSameVersionJDCloudRestoreTask(clusterDto, String.valueOf(sourceRedisUris.toArray()[0])));
        log.info("--------单机到集群-------");
    }



    /**
     * redisCluster同步数据到 JDcloud redis集群
     * @param jdClousterClusterDto
     * @throws TaskMsgException
     */

    @Override
    public void syncToJDCloud(RedisJDClousterClusterDto jdClousterClusterDto) throws TaskMsgException {
        List<String>sourcrList=clusterNodeSettings(jdClousterClusterDto.getJedisAddress(),jdClousterClusterDto.getPassword());
        RedisSyncDataDto syncDataDto=new RedisSyncDataDto(
                "",
                jdClousterClusterDto.getTargetUri(),
                jdClousterClusterDto.getThreadName(),
                jdClousterClusterDto.getMinPoolSize(),
                jdClousterClusterDto.getMaxPoolSize(),
                jdClousterClusterDto.getMaxWaitTime(),
                jdClousterClusterDto.getTimeBetweenEvictionRunsMillis(),
                jdClousterClusterDto.getIdleTimeRunsMillis());


        for(int i=0;i<sourcrList.size();i++){
            RedisSyncDataDto redisSyncDataDto=new RedisSyncDataDto();

            BeanUtils.copyProperties(syncDataDto, redisSyncDataDto);
            redisSyncDataDto.setSourceUri(sourcrList.get(i));

            threadPoolTaskExecutor.execute(new SyncDiffTask(redisSyncDataDto));
        }
    }


    /*
     * zzj add 提出判断连接的部分
     * */
    public void syncTask(RedisSyncDataDto syncDataDto,String sourceUri,String targetUri,boolean status) throws TaskMsgException {
        RedisVersion redisVersion = null;
        try {
            redisVersion =RedisUrlUtils.selectSyncerVersion(sourceUri,targetUri);
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }

        if(status){
            if(TaskMonitorUtils.containsKeyAliveMap(syncDataDto.getThreadName())){
                throw new TaskMsgException(TaskMsgConstant.Task_MSG_PARSE_ERROR_CODE);
            }
        }

        if(redisVersion.equals(RedisVersion.SAME)){
            if(syncDataDto.getPipeline()!=null&&syncDataDto.getPipeline().toLowerCase().equals("on")){
                threadPoolTaskExecutor.execute(new SyncPipeSameTask(syncDataDto));
                log.info("同步同版本（>3.0）版本数据...(开启管道)");
//                log.info("同步不同版本（）版本数据...");
            }else {
                threadPoolTaskExecutor.execute(new SyncSameTask(syncDataDto));
                log.info("同步同版本（>3.0）版本数据...(非管道)");
            }

        }else if(redisVersion.equals(RedisVersion.LOWER)){

            if(syncDataDto.getPipeline()!=null&&syncDataDto.getPipeline().toLowerCase().equals("on")){
                threadPoolTaskExecutor.execute(new SyncPipeSameLowerTask(syncDataDto));
                log.info("同步同版本（<3.0）版本数据...(开启管道)");
//                log.info("同步不同版本（）版本数据...");
            }else {
                threadPoolTaskExecutor.execute(new SyncLowerTask(syncDataDto));
                log.info("同步同版本（<3.0）版本数据...(非管道)");
            }

        }else if(redisVersion.equals(RedisVersion.OTHER)){
            if(syncDataDto.getPipeline()!=null&&syncDataDto.getPipeline().toLowerCase().equals("on")){
                threadPoolTaskExecutor.execute(new SyncPipeDiffTask(syncDataDto));
                log.info("同步不同版本（）版本数据...(开启管道)");
//                log.info("同步不同版本（）版本数据...");
            }else {
                threadPoolTaskExecutor.execute(new SyncDiffTask(syncDataDto));
                log.info("同步不同版本（）版本数据...(非管道)");
            }
        }else {
            log.info("-----其他版本");
            threadPoolTaskExecutor.execute(new SyncTask(syncDataDto));
        }

    }


    public static void main(String[] args) throws Exception {
        String s="47.100.111.210:6380,47.100.111.210:6379,123.206.71.25:6379,115.159.205.140:6380,115.159.205.140:6381,123.207.166.108:6379";


//        IRedisReplicatorService redisReplicatorService = new IRedisReplicatorServiceImpl();
//        redisReplicatorService.backUPRdb("redis://114.67.81.232:6340?authPassword=redistest0102", "D:\\tests");
    }


    /**
     * 获取Node节点uri
     * @param sourceList
     * @param password
     * @return
     */
   synchronized static List<String>clusterNodeSettings(String sourceList,String password){

       List<String>sourceUriList= Arrays.asList(sourceList.split(","));
        List<String> newNodes=new ArrayList<>();
        String psw="";
        if(!StringUtils.isEmpty(password)){
            psw="?authPassword="+password;
        }
        for (String source:sourceUriList) {
            if(!StringUtils.isEmpty(source)){
                String newSource="redis://"+source+psw;
                newNodes.add(newSource);
            }

        }
        return newNodes;
    }


    /**
     * 检查reids是否能够连接
     * @param url
     * @param name
     * @throws TaskMsgException
     */
    void checkRedisUrl(String url,String name) throws TaskMsgException {

        try {
            if(!RedisUrlUtils.checkRedisUrl(url)){
                throw new TaskMsgException("scheme must be [redis].");
            }
            if(!RedisUrlUtils.getRedisClientConnectState(url,name)){
                throw new TaskMsgException(name+" :连接redis失败");
            }
        } catch (URISyntaxException e) {
            throw new TaskMsgException(e.getMessage());
        }
    }
}
