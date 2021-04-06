package syncer.replica.util;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
public enum RedisBranchTypeEnum implements Serializable {
    /**
     * 哨兵
     */
    SENTINEL,
    CLUSTER,FILE,SINGLE,
    /**
     * jimdb
     */
    JIMDB
}
