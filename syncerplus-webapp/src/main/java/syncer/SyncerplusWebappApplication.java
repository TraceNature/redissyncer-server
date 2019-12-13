package syncer;

import redis.clients.jedis.JedisSentinelPool;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;

import syncer.syncerplusredis.util.TaskMsgUtils;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import syncer.syncerservice.persistence.SettingPersistenceTask;
import syncer.syncerservice.util.file.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@ComponentScan(basePackages="syncer")
@EnableScheduling
@EnableCaching  //开启缓存
public class SyncerplusWebappApplication {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        SpringApplication.run(SyncerplusWebappApplication.class, args);

        /**
         * 开启线程监控
         */
//        new Thread(new MinerMonitorThread()).start();
        /**
         * 持久化任务
         */
        new Thread(new SettingPersistenceTask()).start();





        String settingPath = System.getProperty("user.dir") + FileUtils.getSettingName();
        if(FileUtils.existsFile(settingPath)){
//            ConcurrentHashMap<String,ThreadMsgEntity> data=JSON.parseObject(FileUtils.getText(System.getProperty("user.dir")+ FileUtils.getSettingName()),new TypeReference<ConcurrentHashMap<String, ThreadMsgEntity>>() {});
            ConcurrentHashMap<String, ThreadMsgEntity> data= (ConcurrentHashMap<String, ThreadMsgEntity>) FileUtils.FileInputToObject(settingPath);
            for (Map.Entry<String, ThreadMsgEntity> entry:data.entrySet()
                 ) {
                ThreadMsgEntity msgEntity=entry.getValue();
                msgEntity.setRList(new ArrayList<>());
                msgEntity.setStatus(ThreadStatusEnum.STOP);
                data.put(entry.getKey(),msgEntity);
            }
            if(data==null){
                data=new  ConcurrentHashMap<String,ThreadMsgEntity>();
            }
            TaskMsgUtils.setAliveThreadHashMap(data);

        }


    }



    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


}
