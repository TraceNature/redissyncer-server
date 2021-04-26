package syncer.replica.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/18
 */
@AllArgsConstructor
@Getter

public enum RedisBranchType implements Serializable {
    /**
     * 单机
     */
    SINGLE(1, RedisBranchTypeEnum.SINGLE,"单机"),
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
    SENTINEL(4,RedisBranchTypeEnum.SENTINEL,"哨兵"),

    JIMDB(5,RedisBranchTypeEnum.JIMDB,"JIMDB");

    private int code;
    private RedisBranchTypeEnum branchTypeEnum;
    private String msg;
}
