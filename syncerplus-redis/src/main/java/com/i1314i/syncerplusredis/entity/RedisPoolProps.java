package com.i1314i.syncerplusredis.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "syncerplus.redispool")
@Getter@Setter
public class RedisPoolProps {
    private Long timeBetweenEvictionRunsMillis;
    private Long idleTimeRunsMillis;
    private Integer minPoolSize;
    private  Integer maxPoolSize;
    private Long maxWaitTime;
}
