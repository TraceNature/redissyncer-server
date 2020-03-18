package syncer.syncerplusredis.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/18
 */
@AllArgsConstructor
@Getter
public enum RedisBranchType {
    /**
     * 单机
     */
    SINGLE(1,RedisBranchTypeEnum.SINGLE,"单机"),
    /**
     * cluster集群
     */
    CLUSTER(2,RedisBranchTypeEnum.CLUSTER,"cluster集群"),
    /**
     * 文件
     */
    FILE(3,RedisBranchTypeEnum.FILE,"文件"),
    /**
     * 哨兵
     */
    SENTINEL(4,RedisBranchTypeEnum.SENTINEL,"哨兵");
    private int code;
    private RedisBranchTypeEnum branchTypeEnum;
    private String msg;
}
