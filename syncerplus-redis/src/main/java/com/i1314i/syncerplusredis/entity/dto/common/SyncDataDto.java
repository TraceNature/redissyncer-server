package com.i1314i.syncerplusredis.entity.dto.common;

import com.i1314i.syncerplusredis.entity.RedisInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * DTO基础信息
 */
@Getter@Setter
@EqualsAndHashCode
public class SyncDataDto implements Serializable {
    private int minPoolSize;
    private int maxPoolSize;
    private long maxWaitTime;
    private long timeBetweenEvictionRunsMillis;
    private long idleTimeRunsMillis;
    private int diffVersion;
    private String pipeline;
    private Map<Integer,Integer>dbNum;
    private Set<String> sourceUris;
    private Set<String>targetUris;
    private Set<RedisInfo>targetUriData=new HashSet<>();
    private double targetRedisVersion;
    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline,Map<Integer,Integer>dbNum ) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
        this.diffVersion = diffVersion;
        this.pipeline = pipeline;
        this.dbNum=dbNum;
    }

    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis,Map<Integer,Integer>dbNum) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
        this.dbNum=dbNum;
    }

    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
        this.diffVersion = diffVersion;
        this.pipeline = pipeline;

    }

    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;

    }

    public void addRedisInfo(RedisInfo info){
        this.targetUriData.add(info);
    }
//    public Set<RedisInfo> getTargetUriData() {
//        Set<RedisInfo>redisInfoSet=new HashSet<>();
//        for (String uri:targetUris
//             ) {
//            redisInfoSet.add(new RedisInfo(uri));
//        }
//        return targetUriData;
//    }


    public Set<String> getSourceUris() {
        if(null==sourceUris){

        }
        return sourceUris;
    }

    public Set<String> getTargetUris() {
        return targetUris;
    }

    public Set<RedisInfo> getTargetUriData() {
        return targetUriData;
    }

    public Map<Integer, Integer> getDbNum() {
        return dbNum;
    }

    public void setDbNum(Map<Integer, Integer> dbNum) {
        this.dbNum = dbNum;
    }

    public double getTargetRedisVersion() {
        return targetRedisVersion;
    }

    public void setTargetRedisVersion(double targetRedisVersion) {
        this.targetRedisVersion = targetRedisVersion;
    }
}
