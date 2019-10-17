package com.i1314i.syncerplusredis.entity;

import com.i1314i.syncerplusredis.constant.RedisCommandTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PipelineDataEntity {
    private byte[] key;
    private int dbNum;
    private int ttl;
    private RedisCommandTypeEnum redisCommandTypeEnum;

    public PipelineDataEntity(byte[] key, int dbNum, int ttl, RedisCommandTypeEnum redisCommandTypeEnum) {
        this.key = key;
        this.dbNum = dbNum;
        this.ttl = ttl;
        this.redisCommandTypeEnum = redisCommandTypeEnum;
    }
}