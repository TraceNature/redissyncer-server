package syncer.syncerplusredis.entity.muli.multisync;

import lombok.Builder;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/9/15
 */
public class MultiSyncStartDataEntity {
    /**
     * 任务id
     */
    private String taskId;
    /**
     * 任务名称
     */
    private String taskName;

    /**
     * A RedisUri
     */
    private String sourceRedisAddress;

    /**
     * 源用户名
     */
    @Builder.Default
    private String sourceUserName="";


    /**
     * A Redis密码
     */
    private String sourcePassword;


    /**
     * 目标RedisUri
     */
    private String targetRedisAddress;


    /**
     * 目标用户名
     */
    @Builder.Default
    private String targetUserName="";

    /**
     * 目标Redis密码
     */
    private String targetPassword;



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
    @Builder.Default
    private Integer tasktype=1;



    /**
     *增量模式下从缓冲区开始同步的位置
     * 默认为 endbuffer 缓冲区尾
     *
     * "endbuffer"    1
     * "beginbuffer"  2
     *
     */
    @Builder.Default
    private Integer offsetPlace=1;

    /**
     * 任务反馈信息
     */
    @Builder.Default
    private String taskMsg="";

    /**
     * offset地址
     */
    @Builder.Default
    private volatile Long offset=-1L;





}
