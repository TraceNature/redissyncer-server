package syncer;



import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.constant.TaskType;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerservice.persistence.SettingPersistenceTask;
import syncer.syncerservice.persistence.SqliteSettingPersistenceTask;

import java.io.IOException;
import java.util.List;


@SpringBootApplication
@ComponentScan(basePackages="syncer")
@EnableScheduling
@EnableCaching  //开启缓存
@Slf4j
public class SyncerplusWebappApplication {

    public static void main(String[] args) throws IOException, Exception {

        /**
         *
         *
         */


        SpringApplication.run(SyncerplusWebappApplication.class, args);
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


    }



    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    private  static void loadingData() throws Exception {
        TaskMapper taskMapper= SpringUtil.getBean(TaskMapper.class);
        List<TaskModel>taskModelList=taskMapper.selectAll();
        for (TaskModel taskModel:taskModelList){
            if(!taskModel.getStatus().equals(TaskStatusType.BROKEN)||!taskModel.getStatus().equals(TaskStatusType.STOP)){
                taskMapper.updateTaskStatusById(taskModel.getGroupId(),TaskStatusType.STOP.getCode());
            }
        }

        log.info("同步服务初始化状态成功...");
    }


}
