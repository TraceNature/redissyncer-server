package syncerservice.syncerplusservice.service.command;



import syncerservice.syncerplusredis.cmd.impl.DefaultCommand;
import syncerservice.syncerplusredis.entity.dto.common.SyncDataDto;
import syncerservice.syncerplusredis.entity.thread.OffSetEntity;
import syncerservice.syncerplusredis.event.Event;
import syncerservice.syncerplusredis.replicator.Replicator;
import syncerservice.syncerplusservice.pool.ConnectionPool;
import syncerservice.syncerplusservice.pool.RedisClient;
import syncerservice.syncerplusservice.task.CommitSendTask;
import syncerservice.syncerplusservice.util.Jedis.ObjectUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 增量传输
 */
@Slf4j
public class SendDefaultCommand {
    private Lock lock = new ReentrantLock();
    @Getter@Setter
    private String dbindex="-1";


    public void sendDefaultCommand(final OffSetEntity baseOffSet, Event event, Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor, SyncDataDto syncDataDto){
        /**
         * 命令同步
         */
        if (event instanceof DefaultCommand) {


            baseOffSet.setReplId(r.getConfiguration().getReplId());
            baseOffSet.getReplOffset().set(r.getConfiguration().getReplOffset());

            // Step3: sync aof command

            RedisClient redisClient = null;
            try {
                redisClient = pool.borrowResource();
            } catch (Exception e) {
                log.info("命令复制:从池中获取RedisClient失败:{}" , e.getMessage());

            }
            StringBuffer info = new StringBuffer();
            // Step3: sync aof command
            DefaultCommand dc = (DefaultCommand) event;

            if(new String(dc.getCommand()).trim().toUpperCase().equals("SELECT")){
                try {
//                    if(dc.getArgs().length>0){
//                        selectIndex(dc.getArgs()[0]);
//                    }

                    Long dbnum=Long.parseLong(new String(dc.getArgs()[0]));
                    if(syncDataDto.getDbMapper().containsKey(dbnum)){
                        int newdbNum=syncDataDto.getDbMapper().get(dbnum);
                        selectIndex(ObjectUtils.getBytesKey(String.valueOf(newdbNum)));
                    }else {
                        selectIndex(dc.getArgs()[0]);
                    }
                }catch (Exception e){

                }

            }else {
                if(getDbindex().equals("-1")){
                    threadPoolTaskExecutor.submit(new CommitSendTask(dc, redisClient, pool, info,"0"));
                }else {

                    threadPoolTaskExecutor.submit(new CommitSendTask(dc, redisClient, pool, info,getIndex()));
                }

            }
        }
    }

    void selectIndex(byte[]index){
        lock.lock();
        try {
            dbindex=new String(index);
        } catch (Exception e) {

        }finally {
            lock.unlock(); //释放锁
        }
    }

    String getIndex(){
        return dbindex;
    }

}
