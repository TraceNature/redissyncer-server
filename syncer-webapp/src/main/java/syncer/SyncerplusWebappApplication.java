package syncer;



import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.yaml.snakeyaml.Yaml;
import syncer.syncerpluscommon.log.LoggerMessage;
import syncer.syncerpluscommon.log.LoggerQueue;
import syncer.syncerpluscommon.service.SqlFileExecutor;
import syncer.syncerpluscommon.util.db.SqliteUtil;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerpluswebapp.util.EnvironmentUtils;
import syncer.syncerpluswebapp.util.YmlUtils;
import syncer.syncerservice.persistence.SqliteSettingPersistenceTask;
import syncer.syncerpluscommon.util.file.FileUtils;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@SpringBootApplication
@ComponentScan(basePackages={"syncer"})
@EnableScheduling
@EnableCaching  //开启缓存
@Slf4j
@EnableWebSocketMessageBroker
public class SyncerplusWebappApplication {

//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

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

//        SpringApplication.run(SyncerplusWebappApplication.class, args);
//         Map<String, Object> conf = new HashMap<String, Object>();
//
//        URL url = App.class.getResource("classpath:application.yml");
//        System.out.println(url.getContent());
//        Yaml yaml = new Yaml();
//        //通过yaml对象将配置文件的输入流转换成map原始map对象
//        Map map = yaml.loadAs(new FileInputStream(url.getPath()), Map.class);
//        //递归map对象将配置加载到conf对象中
//        YmlUtils.loadRecursion(map, "",conf);
//        System.out.println(map.get("syncer.config.path.logfile"));




//        System.out.println( EnvironmentUtils.searchByKey("syncer.config.path.logfile"));
//        MDC.put("filePath", EnvironmentUtils.searchByKey("syncer.config.path.logfile"));

        if(!FileUtils.existsFile(SqliteUtil.getFilePath())){
            FileUtils.mkdirs(SqliteUtil.getFilePath());
        }
        if(!FileUtils.existsFile(SqliteUtil.getPath())){
            log.info("初始化持久化文件..");
            SqlFileExecutor.execute();
//            SqliteUtil.runSqlScript();
        }else {
            log.info("持久化文件存在,无需初始化持久化文件");
        }


        loadingData();

        /**
         * 开启线程监控
         */
//        new Thread(new MinerMonitorThread()).start();
        /**
         * 持久化任务
         */




        new Thread(new SqliteSettingPersistenceTask()).start();

        String md5A="A239";
        String md5B="B240";



//        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.239:20001?authPassword=redistest0102", "114.67.100.240",20001,md5A,md5B,"A239"));
//        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.240:20001?authPassword=redistest0102","114.67.100.239",20001,md5A,md5B,"B240"));

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
        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
        List<TaskModel>taskModelList=taskMapper.selectAll();
        for (TaskModel taskModel:taskModelList){
            if(!taskModel.getStatus().equals(TaskStatusType.BROKEN.getCode())&&!taskModel.getStatus().equals(TaskStatusType.STOP.getCode())){
                taskMapper.updateTaskStatusById(taskModel.getGroupId(),TaskStatusType.BROKEN.getCode());
            }
        }

        log.info("同步服务初始化状态成功...");
    }


}
