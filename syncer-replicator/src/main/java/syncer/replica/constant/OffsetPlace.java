package syncer.replica.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/17
 */
@AllArgsConstructor
public enum OffsetPlace implements Serializable {
    /**
     *增量模式下从缓冲区开始同步的位置
     * 默认为 endbuffer 缓冲区尾
     *
     * "endbuffer"    1
     * "beginbuffer"  2
     *
     */

    ENDBUFFER(1,"endbuffer","缓冲区尾"),

    BEGINBUFFER(2,"beginbuffer","缓冲区头");
    @Getter
    private int code;
    @Getter
    private String offsetPlace;
    @Getter
    private String msg;
}
