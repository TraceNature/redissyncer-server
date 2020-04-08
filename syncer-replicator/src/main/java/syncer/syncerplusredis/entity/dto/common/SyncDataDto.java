package syncer.syncerplusredis.entity.dto.common;

import syncer.syncerplusredis.constant.RedisType;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.RedisInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;


/**
 * DTO基础信息
 */
@Getter@Setter
@EqualsAndHashCode
public class SyncDataDto implements Serializable {
    private static final long serialVersionUID = -5809782578272943997L;
    private int minPoolSize;
    private int maxPoolSize;
    private long maxWaitTime;
    private long timeBetweenEvictionRunsMillis;
    private long idleTimeRunsMillis;
    private int diffVersion;
    private String pipeline;
    private Map<Integer,Integer>dbMapper;
    private Set<String> sourceUris;
    private Set<String>targetUris;
    private Set<RedisInfo>targetUriData=new HashSet<>();
    private String targetRedisVersion;
    private String fileAddress;
    @Builder.Default
    private  int bigKeySize=8192;
    @Builder.Default
    private String redistype= RedisType.SINGLE.toString();

    //迁移类型：psync/文件
    @Builder.Default
    private FileType fileType=FileType.SYNC;

    public SyncDataDto() {
    }

    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline, Map<Integer,Integer>dbNum ) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
        this.diffVersion = diffVersion;
        this.pipeline = pipeline;
        this.dbMapper=dbNum;
    }

    public SyncDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis,Map<Integer,Integer>dbNum) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.maxWaitTime = maxWaitTime;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        this.idleTimeRunsMillis = idleTimeRunsMillis;
        this.dbMapper=dbNum;
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

    public Set<String> getFileUris() {
        if(null==fileAddress){
            return new HashSet<>();
        }

        return new HashSet<>(Arrays.asList(fileAddress.split(";")));

    }

    public Set<RedisInfo> getTargetUriData() {
        return targetUriData;
    }

    public Map<Integer, Integer> getDbMapper() {
        if(dbMapper!=null){
            return dbMapper;
        }
        return new HashMap<Integer,Integer>();
    }

    public void setDbMapper(Map<Integer, Integer> dbMapper) {
        this.dbMapper = dbMapper;
    }

    public String getTargetRedisVersion() {
        return targetRedisVersion;
    }

    public void setTargetRedisVersion(String targetRedisVersion) {
        this.targetRedisVersion = targetRedisVersion;
    }
}
