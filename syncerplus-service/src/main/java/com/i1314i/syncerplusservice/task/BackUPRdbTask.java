package com.i1314i.syncerplusservice.task;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.io.RawByteListener;

import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import com.moilioncircle.redis.replicator.rdb.skip.SkipRdbVisitor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.Callable;

/**
 * RDB备份
 */
@Slf4j
public class BackUPRdbTask implements Callable<Integer> {
    private String redisPath;
    private String path;


    public BackUPRdbTask(String redisPath, String path) {
        this.redisPath = redisPath;
        this.path = path;
    }

    @Override
    public Integer call() throws Exception {

//        File file = new File(path);
//        if (!file.exists()) {
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                log.info("create new file fail because:{%s}", e.getMessage());
//            }
//        }
//        Replicator replicator;
//        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
//            RawByteListener rawByteListener = new RawByteListener() {
//                @Override
//                public void handle(byte... rawBytes) {
//                    try {
//                        out.write(rawBytes);
//                    } catch (IOException ignore) {
//                    }
//                }
//            };
//            replicator = new RedisReplicator(redisPath);
//            replicator.setRdbVisitor(new SkipRdbVisitor(replicator));
//            replicator.addRdbListener(new RdbListener() {
//                @Override
//                public void preFullSync(Replicator replicator) {
//                    replicator.addRawByteListener(rawByteListener);
//                }
//
//                @Override
//                public void handle(Replicator replicator, KeyValuePair<?> kv) {
//                }
//
//                @Override
//                public void postFullSync(Replicator replicator, long checksum) {
//                    replicator.removeRawByteListener(rawByteListener);
//                    try {
//                        out.close();
//                        replicator.close();
//                    } catch (IOException ignore) {
//
//                    }
//                }
//            });
//            replicator.open();
//        } catch (Exception e) {
//            log.info("[backuUpRdb run error and reason is {%s}]", e.getMessage());
//            return 0;
//        }
        return 1;
    }
}
