package com.i1314i.syncerplusservice.entity.dto.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * DTO基础信息
 */
@Getter@Setter
public class SyncDataDto {
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
    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline,Map<Integer,Integer>dbNum) {
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
}
