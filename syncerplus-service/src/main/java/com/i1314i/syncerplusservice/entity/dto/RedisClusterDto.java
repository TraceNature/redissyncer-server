package com.i1314i.syncerplusservice.entity.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter

public class RedisClusterDto {
    @NotBlank(message = "RedisCluster地址不能为空")
    private String jedisAddress;
    private String password;
    private Integer maxTotal;
    private Integer minIdle;
    private long  timeOut;
    private long connectTimeout;

    public RedisClusterDto(@NotBlank(message = "RedisCluster地址不能为空") String jedisAddress, String password, Integer maxTotal, Integer minIdle, long timeOut, long connectTimeout) {
        this.jedisAddress = jedisAddress;
        this.password = password;
        this.maxTotal = maxTotal;
        this.minIdle = minIdle;
        this.timeOut = timeOut;
        this.connectTimeout = connectTimeout;
    }
}
