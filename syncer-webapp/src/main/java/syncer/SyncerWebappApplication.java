package syncer;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import syncer.common.util.ThreadPoolUtils;
import syncer.common.util.file.FileUtils;
import syncer.common.util.spring.SpringUtil;
import syncer.replica.entity.TaskStatusType;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.mapper.RubbishDataMapper;
import syncer.transmission.mapper.TaskMapper;
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

import java.util.List;
import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages={"syncer","syncer.transmission.mapper"})
@MapperScan("syncer.transmission.mapper")
@EnableScheduling
@EnableCaching  //开启缓存
@Slf4j
public class SyncerWebappApplication {

    public static void main(String[] args) throws Exception {
        System.setProperty("DLog4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");

        SpringApplication application = new SpringApplication(SyncerWebappApplication.class);
        application.addListeners(new ApplicationStartedEventListener());
        application.run(args);

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

                ThreadPoolUtils.shutdown();
            }
        });


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
         * 持久化任务
         */

        loadingData();
        cleanRubbishData();

        ThreadPoolUtils.exec(new SqliteSettingPersistenceTask());
        ThreadPoolUtils.exec(new DbDataCommitTask());
        ThreadPoolUtils.exec(new OffsetCommitTask());

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
                    SpringUtil.getBean(TaskMapper.class).updateTask(model);
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
            if(!taskModel.getStatus().equals(TaskStatusType.BROKEN.getCode())&&!taskModel.getStatus().equals(TaskStatusType.STOP.getCode())){
                SqlOPUtils.updateTaskStatusById(taskModel.getTaskId(),TaskStatusType.BROKEN.getCode());
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

}
