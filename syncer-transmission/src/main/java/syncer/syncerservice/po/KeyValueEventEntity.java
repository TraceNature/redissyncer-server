package syncer.syncerservice.po;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.constant.TaskRunTypeEnum;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.event.Event;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class KeyValueEventEntity implements Serializable {
    private Event event;
    /**
     * 映射之后的dbNum
     */
    private Long dbNum;

    /**
     * 剩余过期时间
     */
    private Long ms;

    /**
     * db映射关系
     */
    private Map<Integer,Integer> dbMapper;

    /**
     * redis版本
     */
    private double redisVersion;

    /**
     * 类型
     */
    private TaskRunTypeEnum taskRunTypeEnum;


    /**
     * 增量offset
     */
    private OffSetEntity baseOffSet;

    //configuration
//    private Configuration configuration;

    /**
     * 服务器id
     */
    private String replId = "?";

    /**
     * offset
     */
    private  Long replOffset = -1L;

}
