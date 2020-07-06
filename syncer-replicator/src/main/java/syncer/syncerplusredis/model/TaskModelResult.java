package syncer.syncerplusredis.model;

import lombok.*;
import syncer.syncerplusredis.constant.*;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/20
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskModelResult implements Serializable {
    /**
     * 任务Id
     */
    private String taskId;
    /**
     * 任务组Id
     */
    private String groupId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 源RedisUri
     */
    private String sourceRedisAddress;

    /**
     * 目标RedisUri
     */
    private String targetRedisAddress;

    /**
     * 文件地址
     */
    private String fileAddress;

    /**
     * 创建任务时是否自动启动
     */
    private boolean autostart;

    /**
     * 进入增量状态后已有OffSet
     * 重新启动时从头开始为 true 续传为false
     */
    private boolean afresh;

    /**
     * 批次大小 默认为 1500
     */
    private Integer batchSize;

    /**
     *任务类型
     * default total   1  全量＋增量
     *  stockonly      2  全量
     *  incrementonly  3  增量
     */

    private TaskType tasktype;


    /**
     * offsetPlace
     */
    private OffsetPlace offsetPlace;

    /**
     * 任务反馈信息
     */
    @Builder.Default
    private String taskMsg="";

    /**
     * offset地址
     */
    @Builder.Default
    private Long offset=-1L;

    /**
     * 任务状态
     *
     * CREATING,CREATE,RUN,STOP,PAUSE,BROKEN
     *
     *  STOP        0      停止
     *  CREATING    1      创建中
     *  CREATE      2      创建完成（完成任务信息校验进入启动阶段）
     *  RUN         3      任务启动完成，进入运行状态
     *  PAUSE       4      任务暂停
     *  BROKEN      5      任务因异常停止
     *  RDBRUNING   6      全量任务进行中
     *  COMMANDRUNING 7    增量任务进行中
     */

    private TaskStatusType status;

    /**
     * redis版本
     */
    private  double redisVersion;


    /**
     * rdb版本
     */
    @Builder.Default
    private Integer rdbVersion=6;

    /**
     * 数据同步类型  ---->SyncType
     * 1 sync
     * 2 rdb
     * 3 aof
     * 4 mixed
     * 5 onlineRdb
     * 6 onlineAof
     * 7 onlineMixed
     * 8 commandDumpUp
     */
    private SyncType syncType;

    private RedisBranchType sourceRedisType;

    private RedisBranchType targetRedisType;

    private Map<Integer,Integer> dbMapper;

    private Map<String,Object> analysisMap;

    private String createTime;

    private String updateTime;

    private String replId;

    /**
     * 全量key的数量
     */
    @Builder.Default
    private Long rdbKeyCount=0L;

    /**
     * 从运行到现在的key总量
     */
    @Builder.Default
    private Long allKeyCount=0L;

    /**
     * 同步到目标的key数量
     */
    @Builder.Default
    private Long realKeyCount=0L;

    /**
     * 同步到目标的key数量
     */
    @Builder.Default
    private Long commandKeyCount=0L;

    private double rate;

    private Integer rate2Int;


    /**
     *上次数据更新间隔时间
     */
    private long lastDataUpdateIntervalTime;

}
