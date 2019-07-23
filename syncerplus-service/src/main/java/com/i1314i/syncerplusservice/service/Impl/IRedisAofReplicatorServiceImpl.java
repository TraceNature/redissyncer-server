package com.i1314i.syncerplusservice.service.Impl;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerplusservice.entity.dto.RedisAofSyncDataDto;

import com.i1314i.syncerplusservice.service.IRedisAofReplicatorService;
import com.i1314i.syncerplusservice.service.dump.CommandKeyValuePair;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;

import com.moilioncircle.redis.replicator.RedisReplicator;
import com.moilioncircle.redis.replicator.Replicator;
import com.moilioncircle.redis.replicator.cmd.Command;

import com.moilioncircle.redis.replicator.cmd.CommandName;
import com.moilioncircle.redis.replicator.cmd.CommandParser;
import com.moilioncircle.redis.replicator.cmd.impl.SelectCommand;
import com.moilioncircle.redis.replicator.cmd.impl.SetCommand;
import com.moilioncircle.redis.replicator.rdb.DefaultRdbVisitor;
import com.moilioncircle.redis.replicator.rdb.datatype.KeyValuePair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
//CommandParsers.toRune;
import com.i1314i.syncerplusservice.cmd.*;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.net.URISyntaxException;


@Slf4j
@Service("redisAofReplicatorService")
public class IRedisAofReplicatorServiceImpl implements IRedisAofReplicatorService {
    private JedisCluster jedisCluster;
    @Override
    public void sync(RedisAofSyncDataDto syncDataDto) throws TaskMsgException, IOException, URISyntaxException {

//        Replicator replicator = new RedisReplicator(syncDataDto.getSourceAofUri());
//        replicator.addCommandListener(new CommandListener() {
//            @Override
//            public void handle(Replicator replicator, Command command) {
//                System.out.println(command);
//            }
//        });
//        replicator.open();
    }




    public static class CatAppendParser implements CommandParser<CatAppendParser.CAppendCommand> {

        @Override
        public CAppendCommand parse(Object[] command) {
            return new CAppendCommand(CommandParsersPlus.toRune(command[1]), CommandParsersPlus.toRune(command[2]));
        }

        public static class CAppendCommand implements Command {
            /**
             *
             */
            private static final long serialVersionUID = 1L;
            public final String key;
            public final String value;

            public CAppendCommand(String key, String value) {
                this.key = key;
                this.value = value;
            }

            @Override
            public String toString() {
                return "YourAppendCommand{" +
                        "key='" + key + '\'' +
                        ", value='" + value + '\'' +
                        '}';
            }
        }
    }

}
