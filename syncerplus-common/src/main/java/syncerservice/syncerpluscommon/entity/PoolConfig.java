package syncerservice.syncerpluscommon.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "syncerplus.poolconfig")
@Getter
@Setter
public class PoolConfig {
    // 核心线程池大小
    private  int corePoolSize;

    // 最大可创建的线程数
    private  int maxPoolSize;

    // 队列最大长度
    private  int queueCapacity;

    // 线程池维护线程所允许的空闲时间
    private  int keepAliveSeconds;
}
