package syncer.syncerpluscommon.util;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/4
 */
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

}
