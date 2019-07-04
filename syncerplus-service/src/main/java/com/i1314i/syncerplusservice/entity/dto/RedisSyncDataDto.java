package com.i1314i.syncerplusservice.entity.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class RedisSyncDataDto {
    @NotBlank(message = "源redis路径地址不能为空")
    private String sourceUri;
    @NotBlank(message = "目标redis路径不能为空")
    private String targetUri;
    @NotBlank(message = "任务名称不能为空")
    private String threadName;

    private int minPoolSize;
    private int maxPoolSize;
    private long maxWaitTime;
    private long timeBetweenEvictionRunsMillis;
    private long idleTimeRunsMillis;



    public RedisSyncDataDto(String sourceUri, String targetUri, String threadName) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadName = threadName;
    }


    public RedisSyncDataDto(@NotBlank(message = "源redis路径地址不能为空") String sourceUri, @NotBlank(message = "目标redis路径不能为空") String targetUri, @NotBlank(message = "任务名称不能为空") String threadName, int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadName = threadName;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
    }
}
