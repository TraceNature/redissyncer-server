package syncer.syncerservice.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/10/9
 */
@Data
@Builder
@AllArgsConstructor
public class FlushCommandStatus {
    /**
     1 flushall
     2 flushdb
     -1 none
     */
    @Builder.Default
    private int type=-1;
    private AtomicBoolean status;
    @Builder.Default
    private int db=-1;
    /**
     * 出现次数
     */
    @Builder.Default
    private AtomicInteger num;
}
