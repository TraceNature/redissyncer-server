package com.i1314i.syncerplusservice.entity.dto;

import lombok.Getter;
import lombok.Setter;

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

    public RedisSyncDataDto(String sourceUri, String targetUri, String threadName) {
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
        this.threadName = threadName;
    }
}
