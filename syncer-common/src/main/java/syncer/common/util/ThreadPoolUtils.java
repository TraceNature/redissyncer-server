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
package syncer.common.util;



import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import syncer.common.config.ThreadPoolConfig;
import syncer.common.util.spring.SpringUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;


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


    public static <S> Future<S> callable(Callable<S> task){
        return threadPoolTaskExecutor.submit(task);
    }

    public static void shutdown(){
        //执行shutdown
        threadPoolTaskExecutor.shutdown();
        log.info("start shutdown....");
    }
}
