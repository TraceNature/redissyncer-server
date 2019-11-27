package syncerservice.syncerplusredis.entity;

import syncerservice.syncerplusredis.cmd.impl.DefaultCommand;
import syncerservice.syncerplusredis.constant.RedisCommandTypeEnum;
import syncerservice.syncerplusredis.rdb.datatype.DB;

import syncerservice.syncerplusredis.entity.thread.EventTypeEntity;

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
