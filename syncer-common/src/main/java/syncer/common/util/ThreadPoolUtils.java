package syncer.common.util;



import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import syncer.common.config.ThreadPoolConfig;
import syncer.common.util.spring.SpringUtil;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/29
 */
@Slf4j
public class ThreadPoolUtils {
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;
    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    public static void exec(Runnable task){

        threadPoolTaskExecutor.execute(task);
    }

    public static void shutdown(){
        //执行shutdown
        threadPoolTaskExecutor.shutdown();
        log.info("start shutdown....");
    }
}
