package syncer.transmission.constants;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/23
 */
public enum RedisCommandTypeEnum implements Serializable {
    STRING,LIST,SET,ZSET,HASH,MODULE,STREAM,DUMP,COMMAND
}
