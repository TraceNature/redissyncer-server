// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;
import syncer.common.config.BreakPointConfig;
import syncer.common.config.EtcdServerConfig;
import syncer.common.constant.StoreType;
import syncer.common.util.ThreadPoolUtils;
import syncer.common.util.file.FileUtils;
import syncer.common.util.spring.SpringUtil;
import syncer.replica.status.TaskStatus;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.heartbeat.DefaultHeartbeatCommandRunner;
import syncer.transmission.heartbeat.Heartbeat;
import syncer.transmission.model.TaskModel;
import syncer.transmission.task.ContextTaskStatus;
import syncer.transmission.task.DbDataCommitTask;
import syncer.transmission.task.OffsetCommitTask;
import syncer.transmission.task.SqliteSettingPersistenceTask;
import syncer.transmission.util.sql.SqlOPUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;
import syncer.webapp.ApplicationStartedEventListener;
import syncer.webapp.executor.SqlFileExecutor;
import syncer.webapp.executor.SqliteUtil;
import syncer.webapp.start.NodeStartCheckResource;
import syncer.webapp.start.NodeStartInitService;

@SpringBootApplication
@ComponentScan(basePackages={"syncer","syncer.transmission.mapper"})
@MapperScan("syncer.transmission.mapper")
@EnableScheduling
@EnableCaching  //开启缓存
@Slf4j
public class SyncerWebappApplication {
    final static String version="v3.3.3";
    public static void main(String[] args) throws Exception {
        System.setProperty("DLog4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        SpringApplication application = new SpringApplication(SyncerWebappApplication.class);
        application.addListeners(new ApplicationStartedEventListener());
        application.run(args);
        log.warn("syncer version:{}",version);
        log.info("client type {}",BreakPointConfig.getBreakpointContinuationType());
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                log.info("Shutdown hook data saving....");
                ContextTaskStatus.STATUS.set(true);
                saveAllData();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
                SqlOPUtils.close();
                ThreadPoolUtils.shutdown();
            }
        });

        EtcdServerConfig config=new EtcdServerConfig();
        //sqlite
        if(StoreType.SQLITE.equals(config.getStoreType())){
            if(!FileUtils.existsFile(SqliteUtil.getFilePath())){
                FileUtils.mkdirs(SqliteUtil.getFilePath());
            }
            if(!FileUtils.existsFile(SqliteUtil.getPath())){
                log.info("initialize data store file...");
                SqlFileExecutor.execute();
            }else {
                log.info("The data store file already exists and does not need to be initialized...");
            }


            /**
             * 节点启动时将本节点非STOP 和 BROKEN状态的节点置为BROKEN 适用于sqlite
             */
            loadingData();
            /**
             * 清理垃圾数据 适用于sqlite
             */
            cleanRubbishData();


            /**
             * 持久化任务
             */
            ThreadPoolUtils.exec(new SqliteSettingPersistenceTask());
            ThreadPoolUtils.exec(new DbDataCommitTask());
            ThreadPoolUtils.exec(new OffsetCommitTask());

        }else{

            //etcd  node heartbeat
            NodeStartCheckResource checkResource=new NodeStartCheckResource();
            try {
                boolean status=checkResource.initCheckResource();
                if(!status){
                    Heartbeat heartbeat=new Heartbeat(10000,new DefaultHeartbeatCommandRunner());
                    heartbeat.heartbeat();
                    NodeStartInitService nodeStartInitService=new NodeStartInitService();
                    nodeStartInitService.initResource();

                    /**
                     * 持久化任务
                     */


                    ThreadPoolUtils.exec(new SqliteSettingPersistenceTask());
                    ThreadPoolUtils.exec(new DbDataCommitTask());
                    ThreadPoolUtils.exec(new OffsetCommitTask());
                }
            }catch (Exception e){

                log.error("start NodeStartCheckResource error {}",e.getMessage());
                SpringUtil.getBean(ShutdownContext.class).showdown();
            }
        }


    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    /**
     * 保存所有数据
     */
    private static void saveAllData(){
        try {
            Map<String, TaskDataEntity> aliveThreadHashMap= SingleTaskDataManagerUtils.getAliveThreadHashMap();
            aliveThreadHashMap.entrySet().forEach(data ->{
                TaskModel model=data.getValue().getTaskModel();
                if(data.getValue().getOffSetEntity()!=null){
                    model.setOffset(data.getValue().getOffSetEntity().getReplOffset().get());
                    if(!StringUtils.isEmpty(data.getValue().getOffSetEntity().getReplId())){
                        model.setReplId(data.getValue().getOffSetEntity().getReplId());
                    }
                }
                try {
                    SqlOPUtils.updateTask(model);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            log.info("同步关闭保存数据成功");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private  static void loadingData() throws Exception {
        List<TaskModel> taskModelList= SqlOPUtils.selectAll();
        for (TaskModel taskModel:taskModelList){
            if(!taskModel.getStatus().equals(TaskStatus.BROKEN.getCode())&&!taskModel.getStatus().equals(TaskStatus.STOP.getCode())&&!taskModel.getStatus().equals(TaskStatus.FINISH.getCode())){
                SqlOPUtils.updateTaskStatusById(taskModel.getTaskId(),TaskStatus.BROKEN.getCode());
            }
        }
        log.info("同步服务初始化状态成功...");
    }

    private static void cleanRubbishData(){
        SqlOPUtils.deleteRubbishDataFromTaskBigKey();
        SqlOPUtils.deleteRubbishDataFromTaskDataAbandonCommand();
        SqlOPUtils.deleteRubbishDataFromTaskDataCompensation();
        SqlOPUtils.deleteRubbishDataFromTaskDataMonitor();
        SqlOPUtils.deleteRubbishDataFromTaskOffSet();
        log.info("End of rubbish data cleaning");
    }

}
