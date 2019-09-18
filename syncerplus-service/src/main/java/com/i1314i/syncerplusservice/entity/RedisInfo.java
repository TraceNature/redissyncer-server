package com.i1314i.syncerplusservice.entity;


import com.i1314i.syncerplusredis.entity.RedisURI;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.URISyntaxException;


public class RedisInfo implements Serializable {
    @Getter@Setter
    private double redisVersion;
    @Getter@Setter
    private String uri;
    @Getter@Setter
    private int rdbVersion;
    private RedisURI redisURI;

    public RedisInfo(double redisVersion, String uri, int rdbVersion, RedisURI redisURI) {
        this.redisVersion = redisVersion;
        this.uri = uri;
        this.rdbVersion = rdbVersion;
        this.redisURI = redisURI;
    }

    public RedisInfo(double redisVersion, String uri, int rdbVersion) {
        this.redisVersion = redisVersion;
        this.uri = uri;
        this.rdbVersion = rdbVersion;
    }

    public RedisURI getRedisURI() {
        if (redisURI == null) {
            if(StringUtils.isEmpty(uri)){
                try {
                    redisURI=new RedisURI(uri);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return redisURI;
    }


}
