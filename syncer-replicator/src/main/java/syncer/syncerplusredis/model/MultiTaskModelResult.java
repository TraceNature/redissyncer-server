package syncer.syncerplusredis.model;

import lombok.*;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

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
public class MultiTaskModelResult implements Serializable {

    /**
     * 双向任务Id
     */
    private String taskId;
    /**
     * 同步任务id
     */
    private String parentId;

    /**
     * 任务节点Id
     */
    private String nodeId;

    /**
     * 任务名称
     */
    private String taskName;


    /**
     * redis host
     */
    private String host;


    /**
     * acl
     */
    @Builder.Default
    private boolean acl=false;

    /**
     * 用户名
     */
    @Builder.Default
    private String userName="";


    /**
     * 端口
     */
    @Builder.Default
    private Integer port=6379;

    /**
     * 错误数据总数  30L
     */
    @Builder.Default
    private volatile Long errorCount=1L;

    /**
     * 被抛弃key阈值
     */
    @Builder.Default
    private AtomicLong errorNums = new AtomicLong(0L);


    /**
     * 目标Redis类型
     * 1 单机
     * 2 cluster
     */
    @Builder.Default
    private Integer targetRedisType=1;

    /**
     * md5
     */
    private String md5;

    /**
     * 全量数据分析报告
     */
    private String dataAnalysis;

    /**
     * replId
     */
    @Builder.Default
    private String replId="";

    /**
     * offset地址
     */
    @Builder.Default
    private volatile Long offset=-1L;

    /**
     * 任务状态
     *
     * CREATING,CREATE,RUN,STOP,PAUSE,BROKEN
     *
     *  STOP        0      停止
     *  CREATING    1      创建中
     *  CREATED      2      创建完成（完成任务信息校验进入启动阶段）
     *  RUN         3      任务启动完成，进入运行状态
     *  PAUSE       4      任务暂停
     *  BROKEN      5      任务因异常停止
     *  RDBRUNING   6      全量任务进行中
     *  COMMANDRUNING 7    增量任务进行中
     */
    @Builder.Default
    private Integer status=0;

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
     * 1 sync (默认双活支持)
     * 2 rdb
     * 3 aof
     * 4 mixed
     * 5 onlineRdb
     * 6 onlineAof
     * 7 onlineMixed
     * 8 commandDumpUp
     */
    @Builder.Default
    private Integer syncType=1;


    /**
     * db库映射关系
     */
    private String dbMapper;
    /**
     * 子任务创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;


    /**
     * 全量key的数量
     */
    @Builder.Default
    private volatile Long rdbKeyCount=0L;

    /**
     * 从运行到现在的key总量
     */
    @Builder.Default
    private volatile Long allKeyCount=0L;

    /**
     * 同步到目标的key数量
     */
    @Builder.Default
    private volatile Long realKeyCount=0L;

    /**
     * 当前Key的最后一次更新时间（数据流入）
     */
    private volatile  long lastKeyUpdateTime=0L;

    /**
     * 当前Key的最后一次pipeline提交时间（数据流出）
     */
    private volatile long lastKeyCommitTime=0L;

}
