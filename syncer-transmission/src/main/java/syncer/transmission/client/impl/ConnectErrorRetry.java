package syncer.transmission.client.impl;

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.exceptions.JedisConnectionException;

import java.util.Objects;

/**
 * 连接重试机制
 */
@Slf4j
public class ConnectErrorRetry {
    public static final int MAX_TIMES=5;
    private String taskId;
    private boolean closeStatus=false;
    //2n-1
    public ConnectErrorRetry(String taskId) {
        this.taskId = taskId;
    }

    public void retry(JedisRetryRunner retryRunner){
        int times = 0;
        JedisConnectionException ret=null;
        while (times++<MAX_TIMES){
            try {
                if(closeStatus){
                    log.info("[TASKID {}],ConnectErrorRetry send close event");
                    break;
                }
                log.error("[TASKID {}],send target retry {} times",taskId,times);
                retryRunner.run();
                return;
            }catch (JedisConnectionException e){
                ret=e;
            }

            try {
                Thread.sleep((2*times-1)*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(Objects.nonNull(ret)){
            throw ret;
        }
    }

    public void close(){
        closeStatus=true;
    }
}
