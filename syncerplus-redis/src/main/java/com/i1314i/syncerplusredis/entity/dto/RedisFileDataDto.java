package com.i1314i.syncerplusredis.entity.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;


/**
 * RDB AOF文件同步配置
 */
@Getter
@Setter
@EqualsAndHashCode
public class RedisFileDataDto {
    private static final long serialVersionUID = -5809782578272943998L;
    @NotBlank(message = "AOF/RDB 地址不能为空")
    private String fileAddress;
    @NotBlank(message = "目标RedisCluster地址不能为空")
    private String targetRedisAddress;
    private String targetPassword;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    @Builder.Default
    private boolean autostart=false;
    private int batchSize;
}
