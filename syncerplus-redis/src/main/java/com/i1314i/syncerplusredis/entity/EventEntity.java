package com.i1314i.syncerplusredis.entity;

import com.i1314i.syncerplusredis.cmd.impl.DefaultCommand;
import com.i1314i.syncerplusredis.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusredis.rdb.datatype.DB;

import com.i1314i.syncerplusredis.entity.thread.EventTypeEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter@Setter
@EqualsAndHashCode
public class EventEntity implements Serializable {
    private byte[]key;
    private long ms;
    private DB db;
    private EventTypeEntity typeEntity;
    private RedisCommandTypeEnum redisCommandTypeEnum;
    private DefaultCommand command;
    public EventEntity(byte[] key, long ms,DB db, EventTypeEntity typeEntity,RedisCommandTypeEnum redisCommandTypeEnum) {
        this.key = key;
        this.ms = ms;
        this.db=db;
        this.typeEntity = typeEntity;
        this.redisCommandTypeEnum =redisCommandTypeEnum;
    }

//    public EventEntity(byte[] key,DB db, EventTypeEntity typeEntity,RedisCommandTypeEnum redisCommandTypeEnum) {
//        this.key = key;
//        this.db=db;
//        this.typeEntity = typeEntity;
//        this.redisCommandTypeEnum =redisCommandTypeEnum;
//    }

    public EventEntity(DB db, EventTypeEntity typeEntity, RedisCommandTypeEnum redisCommandTypeEnum, DefaultCommand command) {
        this.db = db;
        this.typeEntity = typeEntity;
        this.redisCommandTypeEnum = redisCommandTypeEnum;
        this.command = command;
    }
}
