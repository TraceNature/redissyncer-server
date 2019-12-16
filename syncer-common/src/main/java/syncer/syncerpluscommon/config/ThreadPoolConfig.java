package syncer.syncerpluscommon.config;

import syncer.syncerpluscommon.entity.PoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig
{
    @Autowired
    private PoolConfig poolConfig;
//    // 核心线程池大小
//    private static int corePoolSize = 50;
//
//    // 最大可创建的线程数
//    private static int maxPoolSize = 200;
//
//    // 队列最大长度
//    private static int queueCapacity = 1000;
//
//    // 线程池维护线程所允许的空闲时间
//    private static int keepAliveSeconds = 300;



    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

//        executor.setMaxPoolSize(maxPoolSize);
//        executor.setCorePoolSize(corePoolSize);
//        executor.setQueueCapacity(queueCapacity);
//        executor.setKeepAliveSeconds(keepAliveSeconds);

        executor.setMaxPoolSize(poolConfig.getMaxPoolSize());
        executor.setCorePoolSize(poolConfig.getCorePoolSize());
        executor.setQueueCapacity(poolConfig.getQueueCapacity());
        executor.setKeepAliveSeconds(poolConfig.getKeepAliveSeconds());
        // 线程池对拒绝任务(无线程可用)的处理策略
        // 执行任务（这个策略重试添加当前的任务，他会自动重复调用 execute() 方法，直到成功） 如果执行器已关闭,则丢弃.

        /**
         * 1： AbortPolicy 丢弃任务，抛运行时异常
         * 2：CallerRunsPolicy 执行任务（这个策略重试添加当前的任务，他会自动重复调用 execute() 方法，直到成功） 如果执行器已关闭,则丢弃.
         * 3：DiscardPolicy 对拒绝任务直接无声抛弃，没有异常信息
         * 4：DiscardOldestPolicy 对拒绝任务不抛弃，而是抛弃队列里面等待最久的（队列头部的任务将被删除）一个线程，然后把拒绝任务加到队列（Queue是先进先出的任务调度算法，具体策略会咋下面有分析）（如果再次失败，则重复此过程）
         * 5：实现RejectedExecutionHandler接口，可自定义处理器（可以自己实现然后set进去）
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }


    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService()
    {
        return  Executors.newScheduledThreadPool(poolConfig.getCorePoolSize());

    }


}