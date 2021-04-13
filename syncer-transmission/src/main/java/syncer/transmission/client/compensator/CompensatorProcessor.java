package syncer.transmission.client.compensator;

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.Jedis;
import syncer.jedis.params.SetParams;
import syncer.replica.util.strings.Strings;
import syncer.transmission.cmd.JedisProtocolCommand;
import syncer.transmission.compensator.PipeLineCompensatorEnum;
import syncer.transmission.entity.EventEntity;
import syncer.transmission.util.object.ObjectUtils;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.concurrent.locks.Lock;

@Slf4j
public class CompensatorProcessor {
    private String taskId;
    /**
     * 提交锁
     */
    private Lock commitLock;




    Object sendMI(Object data, EventEntity eventEntity, Jedis client) {
        long pttl = eventEntity.getMs();
        Object result = "OK";
        if (pttl > 0L) {
            result = client.set(eventEntity.getStringKey(), String.valueOf(data), SetParams.setParams().px(pttl));
        } else {
            long targetPttl = client.pttl(eventEntity.getStringKey());
            if (targetPttl > 0) {
                result = client.set(eventEntity.getStringKey(), String.valueOf(data), SetParams.setParams().px(targetPttl));
            } else {
                result = client.set(eventEntity.getStringKey(), String.valueOf(data));
            }
        }
        return result;
    }


}
