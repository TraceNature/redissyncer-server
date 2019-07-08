package com.i1314i.syncerplusservice.util;

import com.i1314i.syncerplusservice.pool.RedisClient;
import com.i1314i.syncerplusservice.service.exception.TaskMsgException;
import com.moilioncircle.redis.replicator.Configuration;
import com.moilioncircle.redis.replicator.RedisURI;
import redis.clients.jedis.exceptions.JedisDataException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

import static redis.clients.jedis.Protocol.Command.AUTH;

public class RedisUrlUtils {
    public static boolean checkRedisUrl(String uri) throws URISyntaxException {
        URI uriplus = new URI(uri);
        if (uriplus.getScheme() != null && uriplus.getScheme().equalsIgnoreCase("redis")) {
            return true;
        }
        return false;
    }

    public static boolean getRedisClientConnectState(String url,String name) throws TaskMsgException{
        RedisURI turi = null;
        RedisClient target = null;
        try {
            turi = new RedisURI(url);

             target = new RedisClient(turi.getHost(), turi.getPort());
            Configuration tconfig = Configuration.valueOf(turi);
            //获取password
            if (tconfig.getAuthPassword() != null) {
                Object auth = target.send(AUTH, tconfig.getAuthPassword().getBytes());
            }



            try {
                target.send("GET".getBytes(),"TEST".getBytes());
                return true;
            }catch (Exception e){
                return false;
            }

        } catch (URISyntaxException e) {
            throw  new TaskMsgException(name+":连接链接不正确");
        }catch (JedisDataException e){
            throw  new TaskMsgException(name+":"+e.getMessage());
        } finally {
            if(null!=target){
                target.close();
            }
        }

    }


    public static void main(String[] args) throws TaskMsgException {
        getRedisClientConnectState("redis://114.67.81.232:6340?authPassword=redistest010","redis");
    }

}
