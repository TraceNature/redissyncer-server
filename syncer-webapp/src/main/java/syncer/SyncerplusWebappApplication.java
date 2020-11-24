package syncer;



import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import syncer.syncerpluscommon.service.SqlFileExecutor;
import syncer.syncerpluscommon.util.ThreadPoolUtils;
import syncer.syncerpluscommon.util.db.SqliteUtil;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.RubbishDataMapper;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerservice.MultiMasterReplication.multiSync.RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask;
import syncer.syncerservice.persistence.SqliteSettingPersistenceTask;
import syncer.syncerpluscommon.util.file.FileUtils;
import syncer.syncerplusredis.entity.muli.multisync.MultiTaskModel;
import syncer.syncerservice.po.FlushCommandStatus;
import syncer.syncerservice.task.OffsetCommitTask;
import syncer.syncerservice.util.circle.MultiSyncCircle;
import syncer.syncerservice.util.jedis.StringUtils;
import syncer.syncerservice.util.taskutil.taskServiceQueue.DbDataCommitTask;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@SpringBootApplication
@ComponentScan(basePackages={"syncer"})
@EnableScheduling
@EnableCaching  //开启缓存
@Slf4j
@EnableWebSocketMessageBroker
public class SyncerplusWebappApplication {

//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//    @Autowired
//    ThreadPoolTaskExecutor threadPoolTaskExecutor;


//    int info=1;
//    @Scheduled(fixedRate = 1000)
//    public void outputLogger(){
//        log.info("测试日志输出"+info++);
//    }
    /**
     * 推送日志到/topic/pullLogger
     */
/**
    @PostConstruct
    public void pushLogger(){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        LoggerMessage log = LoggerQueue.getInstance().poll();
                        if(log!=null){
                            if(messagingTemplate!=null){
                                messagingTemplate.convertAndSend("/topic/pullLogger",log);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        threadPoolTaskExecutor.submit(runnable);
    }

**/

    public static void main(String[] args) throws  Exception {
        System.setProperty("DLog4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        /**
         *
         *
         *
         */

        SpringApplication application = new SpringApplication(SyncerplusWebappApplication.class);
        application.addListeners(new ApplicationStartedEventListener());
        application.run(args);




        /**

        String aParentId="111";
        String bParentId="222";
        String aNodeId="1";
        String bNodeId="2";
        String taskId="11111";

        MultiTaskModel sourcemultiTaskModel=MultiTaskModel.builder().redisAddress("redis://114.67.100.239:6379?authPassword=redistest0102")
                .nodeId(aNodeId).taskId(taskId).parentId(aParentId).host("114.67.100.240").status(3).password("redistest0102").port(6379).build();
        MultiTaskModel  targetmultiTaskModel=MultiTaskModel.builder().redisAddress("redis://114.67.100.240:6379?authPassword=redistest0102").status(3)
                .nodeId(bNodeId)  .taskId(taskId).parentId(bParentId).host("114.67.100.239").password("redistest0102").port(6379).build();

        ParentMultiTaskModel parentMultiTaskModel=ParentMultiTaskModel.builder().taskId(taskId).taskName(taskId)
                .redisNodeA(Arrays.asList(sourcemultiTaskModel))
                .redisNodeB(Arrays.asList(targetmultiTaskModel))
                .build();
        MulitSyncTaskManagerutils.addTask(parentMultiTaskModel);
        Map<String,Map<String, AtomicLong>>nodeGroupData=new ConcurrentHashMap<>();
        nodeGroupData.put(aNodeId,new ConcurrentHashMap<String, AtomicLong>());
        nodeGroupData.put(bNodeId,new ConcurrentHashMap<String, AtomicLong>());


        Map<String, FlushCommandStatus>flushCommandStatus=new ConcurrentHashMap<>();
        flushCommandStatus.put(aNodeId, FlushCommandStatus.builder().type(-1).num(new AtomicInteger(0)).db(-1).status(new AtomicBoolean(false)).build());
        flushCommandStatus.put(bNodeId,FlushCommandStatus.builder().type(-1).num(new AtomicInteger(0)).db(-1).status(new AtomicBoolean(false)).build());

        Map<String, AtomicInteger>dbData=new ConcurrentHashMap<>();
        dbData.put(aNodeId,new AtomicInteger(0));
        dbData.put(bNodeId,new AtomicInteger(0));

        MultiSyncCircle circle=MultiSyncCircle.builder().nodeGroupData(nodeGroupData)
                .dbData(dbData)
                .flushCommandStatus(flushCommandStatus)
                .nodeCount(2)
                .nodeStatus(new AtomicInteger(0))
                .nodeSuccessStatus(new AtomicBoolean(true))
                .build();
//        new Thread(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(targetmultiTaskModel,"222",circle)).start();
        ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(sourcemultiTaskModel,aNodeId,bNodeId,circle));
        ThreadPoolUtils.exec(new RedisMultiSyncBreakingRingByAuxiliaryKeyTransmissionTask(targetmultiTaskModel,bNodeId,aNodeId,circle));


         */
//        new Thread(()->{
//            while (true){
//                System.out.println(JSON.toJSONString(nodeGroupData));
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }).start();


        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                log.info("Shutdown hook data saving....");
                saveAllData();
                ThreadPoolUtils.shutdown();
            }
        });

        if(!FileUtils.existsFile(SqliteUtil.getFilePath())){
            FileUtils.mkdirs(SqliteUtil.getFilePath());
        }
        if(!FileUtils.existsFile(SqliteUtil.getPath())){
            log.info("initialize data store file...");
            SqlFileExecutor.execute();
//            SqliteUtil.runSqlScript();
        }else {
            log.info("The data store file already exists and does not need to be initialized...");
        }


        loadingData();
        cleanRubbishData();

        /**
         * 开启线程监控
         */
//        new Thread(new MinerMonitorThread()).start();
        /**
         * 持久化任务
         */


        ThreadPoolUtils.exec(new SqliteSettingPersistenceTask());
        ThreadPoolUtils.exec(new DbDataCommitTask());
        ThreadPoolUtils.exec(new OffsetCommitTask());
//        String logFilePath=EnvironmentUtils.searchByKey("syncer.config.path.logfile")+"/"+EnvironmentUtils.searchByKey("syncer.config.path.logfileName");
//        ThreadPoolUtils.exec(new TailLogThread(logFilePath));
        String md5A="A239";
        String md5B="B240";



//        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.239:6379?authPassword=redistest0102", "114.67.100.240",6379,md5A,md5B,"A239",1));
//        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.240:6379?authPassword=redistest0102","114.67.100.239",6379,md5A,md5B,"B240",2));

//        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://10.0.1.45:20001?authPassword=redistest0102","10.0.1.46",20001,md5A,md5B,"A239"));
//        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://10.0.1.46:20001?authPassword=redistest0102","10.0.1.45",20001,md5A,md5B,"B240"));
//        threadA.start();
//        threadB.start();

//        String settingPath = System.getProperty("user.dir") + FileUtils.getSettingName();
//        if(FileUtils.existsFile(settingPath)){
////            ConcurrentHashMap<String,ThreadMsgEntity> data=JSON.parseObject(FileUtils.getText(System.getProperty("user.dir")+ FileUtils.getSettingName()),new TypeReference<ConcurrentHashMap<String, ThreadMsgEntity>>() {});
//            ConcurrentHashMap<String, ThreadMsgEntity> data= (ConcurrentHashMap<String, ThreadMsgEntity>) FileUtils.FileInputToObject(settingPath);
//            for (Map.Entry<String, ThreadMsgEntity> entry:data.entrySet()
//                 ) {
//                ThreadMsgEntity msgEntity=entry.getValue();
//                msgEntity.setRList(new ArrayList<>());
//                msgEntity.setStatus(ThreadStatusEnum.STOP);
//                data.put(entry.getKey(),msgEntity);
//            }
//            if(data==null){
//                data=new  ConcurrentHashMap<String,ThreadMsgEntity>(10);
//            }
//            TaskMsgUtils.setAliveThreadHashMap(data);
//
//        }

//
//        while(true){
//            log.warn("---------------------------------------------------------------------------------------------------------------------------------------");
//        }
    }



    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    private  static void loadingData() throws Exception {
        List<TaskModel>taskModelList=SqliteOPUtils.selectAll();
        for (TaskModel taskModel:taskModelList){
            if(!taskModel.getStatus().equals(TaskStatusType.BROKEN.getCode())&&!taskModel.getStatus().equals(TaskStatusType.STOP.getCode())){
                SqliteOPUtils.updateTaskStatusById(taskModel.getTaskId(),TaskStatusType.BROKEN.getCode());
            }
        }

        log.info("同步服务初始化状态成功...");
    }

    private static void cleanRubbishData(){
        RubbishDataMapper rubbishDataMapper=SpringUtil.getBean(RubbishDataMapper.class);
        rubbishDataMapper.deleteRubbishDataFromTaskBigKey();
        rubbishDataMapper.deleteRubbishDataFromTaskDataAbandonCommand();
        rubbishDataMapper.deleteRubbishDataFromTaskDataCompensation();
        rubbishDataMapper.deleteRubbishDataFromTaskDataMonitor();
        rubbishDataMapper.deleteRubbishDataFromTaskOffSet();
        log.info("End of rubbish data cleaning");
    }


    /**
     * 保存所有数据
     */
    private static void saveAllData(){
        try {
            Map<String, TaskDataEntity> aliveThreadHashMap= TaskDataManagerUtils.getAliveThreadHashMap();

            aliveThreadHashMap.entrySet().forEach(data ->{

                TaskModel model=data.getValue().getTaskModel();
                if(data.getValue().getOffSetEntity()!=null){
                    model.setOffset(data.getValue().getOffSetEntity().getReplOffset().get());
                    if(!StringUtils.isEmpty(data.getValue().getOffSetEntity().getReplId())){
                        model.setReplId(data.getValue().getOffSetEntity().getReplId());
                    }
                }

                try {
                    SpringUtil.getBean(TaskMapper.class).updateTask(model);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            log.info("同步关闭保存数据成功");
        }catch (Exception e){

        }
    }
}
