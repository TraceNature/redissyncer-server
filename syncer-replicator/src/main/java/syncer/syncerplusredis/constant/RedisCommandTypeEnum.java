package syncer.syncerplusredis.constant;


import java.io.Serializable;

public enum RedisCommandTypeEnum implements Serializable {
    STRING,LIST,SET,ZSET,HASH,MODULE,STREAM,DUMP,COMMAND
}
