package com.i1314i.syncerplusservice.task;

import com.i1314i.syncerplusservice.pool.ConnectionPool;
import com.i1314i.syncerplusservice.pool.RedisClient;
import com.moilioncircle.redis.replicator.cmd.impl.DefaultCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class CommitSendTask implements Callable<Object> {

    private DefaultCommand command;
    private RedisClient redisClient;
    private ConnectionPool pool;
    private StringBuffer info;

    public CommitSendTask(DefaultCommand command, RedisClient redisClient, ConnectionPool pool) {
        this.command = command;
        this.redisClient = redisClient;
        this.pool = pool;
        this.info=new StringBuffer();
    }

    public CommitSendTask(DefaultCommand command, RedisClient redisClient, ConnectionPool pool, StringBuffer info) {
        this.command = command;
        this.redisClient = redisClient;
        this.pool = pool;
        this.info = info;
    }

    /**
     * 缺少校验
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        Object r = redisClient.send(command.getCommand(), command.getArgs());
        pool.release(redisClient);
        info.append(new String(command.getCommand()));
        info.append(":");
        for (byte[] arg : command.getArgs()) {
            info.append("[");
            info.append(new String(arg));
            info.append("]");
        }

        info.append("->");
        info.append(r);
        log.info(info.toString());
        return r;
    }
}
