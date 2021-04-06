package syncer.replica.util;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */
public enum  ThreadStatusEnum implements Serializable {
    /**
     * 创建中...
     */
    CREATING,
    CREATED,
    RUN,
    STOP,
    PAUSE,
    BROKEN,
    RDBRUNING,
    /**
     *
     */
    COMMANDRUNING
}
