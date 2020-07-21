package syncer.syncerplusredis.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;
import syncer.syncerplusredis.entity.FileType;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/15
 */
@AllArgsConstructor

public enum TargetAndSourceRedisType implements Serializable {
    /**
     * 单机
     */
    Single(1,RedisType.SINGLE),

    /**
     * 集群
     */
    Cluster(2,RedisType.CLUSTER),

    /**
     * 文件
     */
    File(3, RedisType.FILE);
    @Getter
    private Integer code;
    @Getter
    private RedisType redisType;

}
