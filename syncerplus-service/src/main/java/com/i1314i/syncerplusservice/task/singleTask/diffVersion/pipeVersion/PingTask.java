package com.i1314i.syncerplusservice.task.singleTask.diffVersion.pipeVersion;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
@Slf4j
public class PingTask implements Runnable {
    private Jedis targetJedisplus;

    public PingTask(Jedis targetJedisplus) {
        this.targetJedisplus = targetJedisplus;
    }

    @Override
    public void run() {
        while (targetJedisplus.isConnected()){
            String r=targetJedisplus.ping();
            log.info(r);
        }

    }
}
