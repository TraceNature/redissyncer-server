package syncer.syncerservice.util.circle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 双向同步命令记录
 * @Date 2020/9/17
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MultiSyncCommands {
    public  Map<String, AtomicLong> aNodeGroup=new ConcurrentHashMap<>();
    public  Map<String, AtomicLong> bNodeGroup=new ConcurrentHashMap<>();
}
