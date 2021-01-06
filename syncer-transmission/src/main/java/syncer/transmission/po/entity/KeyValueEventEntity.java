package syncer.transmission.po.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.entity.FileType;
import syncer.replica.entity.TaskRunTypeEnum;
import syncer.replica.event.Event;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.entity.OffSetEntity;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
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

    /**
     * fileType
     */
    private FileType fileType= FileType.SYNC;

    /**
     * 补偿机制
     */
    private ISyncerCompensator iSyncerCompensator;

    /**
     * 批次总大小
     */
    private KeyValueSizeEntity keyValueSizeEntity;



}