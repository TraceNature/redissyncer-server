package syncer.syncerplusservice.service.Impl;

import syncer.syncerplusredis.cmd.Command;
import syncer.syncerplusredis.cmd.CommandParser;
import syncer.syncerplusredis.entity.dto.RedisAofSyncDataDto;

import syncer.syncerplusservice.service.IRedisAofReplicatorService;
import syncer.syncerplusredis.exception.TaskMsgException;


import syncer.syncerplusservice.cmd.CommandParsersPlus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
//CommandParsers.toRune;
import syncer.syncerplusservice.cmd.*;
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
