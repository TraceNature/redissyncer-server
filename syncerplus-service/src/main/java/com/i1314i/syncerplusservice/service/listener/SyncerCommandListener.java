package com.i1314i.syncerplusservice.service.listener;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.moilioncircle.redis.replicator.Replicator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Slf4j
/**
 * 命令复制
 */
public class SyncerCommandListener  {
    private Replicator r;
    private ConnectionPool pool;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public SyncerCommandListener(Replicator r, ConnectionPool pool, ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        this.r = r;
        this.pool = pool;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public void run() {

        /**

        try {

            r.addCommandListener(new com.moilioncircle.redis.replicator.cmd.CommandListener() {
                @Override
                public void handle(Replicator replicator, Command command) {
                    RedisUrlUtils.doCommandCheckTask(r);
                    if (RedisUrlUtils.doThreadisCloseCheckTask()) {
                        return;
                    }
                    if (!(command instanceof DefaultCommand)) return;

                    RedisClient redisClient = null;
                    try {
                        redisClient = pool.borrowResource();
                    } catch (Exception e) {
                        log.info("命令复制:从池中获取RedisClient失败:" + e.getMessage());

                    }
                    StringBuffer info = new StringBuffer();
                    // Step3: sync aof command
                    DefaultCommand dc = (DefaultCommand) command;

                    threadPoolTaskExecutor.submit(new CommitSendTask(dc, redisClient, pool, info));

                }
            });
            r.addCloseListener(new CloseListener() {
                @Override
                public void handle(Replicator replicator) {
//                    if(null!=pool)
//                    pool.close();
                }
            });

            r.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
         **/
    }


}
