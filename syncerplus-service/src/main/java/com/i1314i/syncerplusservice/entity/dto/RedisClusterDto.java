package com.i1314i.syncerplusservice.entity.dto;

import com.i1314i.syncerplusservice.entity.dto.common.SyncDataDto;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 新版dto
 */
@Getter
@Setter

public class RedisClusterDto extends SyncDataDto {
    @NotBlank(message = "源RedisCluster地址不能为空")
    private String sourceRedisAddress;
    @NotBlank(message = "目标RedisCluster地址不能为空")
    private String targetRedisAddress;
    private String sourcePassword;
    private String targetPassword;
    @NotBlank(message = "任务名称不能为空")
    private String threadName;




    public RedisClusterDto(@NotBlank(message = "源RedisCluster地址不能为空") String sourceRedisAddress, @NotBlank(message = "目标RedisCluster地址不能为空") String targetRedisAddress, String sourcePassword, String targetPassword, @NotBlank(message = "任务名称不能为空") String threadName, int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline) {
        super(minPoolSize,maxPoolSize,maxWaitTime,timeBetweenEvictionRunsMillis,idleTimeRunsMillis,diffVersion,pipeline);
        this.sourceRedisAddress = sourceRedisAddress;
        this.targetRedisAddress = targetRedisAddress;
        this.sourcePassword = sourcePassword;
        this.targetPassword = targetPassword;
        this.threadName = threadName;

    }

}
