package com.i1314i.syncerplusservice.entity.dto;

import com.i1314i.syncerplusservice.entity.dto.common.SyncDataDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;

@Getter
@Setter
public class RedisSyncDataDto extends SyncDataDto {
    @NotBlank(message = "源redis路径地址不能为空")
    private String sourceUri;
    @NotBlank(message = "目标redis路径不能为空")
    private String targetUri;
    @NotBlank(message = "任务名称不能为空")
    private String threadName;
    private double redisVersion;
    private int rdbVersion;

    public RedisSyncDataDto() {
        super(1, 100, 3000, 15000, 15000, 1, "off",new HashMap<>());
    }

    public RedisSyncDataDto(@NotBlank(message = "源redis路径地址不能为空") String sourceUri, @NotBlank(message = "目标redis路径不能为空") String targetUri, @NotBlank(message = "任务名称不能为空") String threadName, int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
        super(minPoolSize,maxPoolSize,maxWaitTime,timeBetweenEvictionRunsMillis,idleTimeRunsMillis);
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadName = threadName;
    }

    public double getRedisVersion() {
        return redisVersion;
    }

    public void setRedisVersion(double redisVersion) {
        this.redisVersion = redisVersion;
    }

    public int getRdbVersion() {
        return rdbVersion;
    }

    public void setRdbVersion(int rdbVersion) {
        this.rdbVersion = rdbVersion;
    }
}
