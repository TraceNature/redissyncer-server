package com.i1314i.syncerplusredis.entity.thread;



import com.i1314i.syncerplusredis.constant.ThreadStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@Builder
@AllArgsConstructor

@EqualsAndHashCode

public class ThreadReturnMsgEntity {
    private String id;
    private String taskName;
    private ThreadStatusEnum status;
//    private RedisClusterDto redisClusterDto;
    private String sourceRedisAddress;
    private String targetRedisAddress;
    private boolean afresh;
    private Map<Integer,Integer> dbNum;
    private Set<String> sourceUris;
    private Set<String>targetUris;
    private double targetRedisVersion;
    public Set<String> getSourceUris() {
        Set<String>sourceUri=new HashSet<>();
        for (String data:sourceUris
        ) {
            sourceUri.add(data.split("\\?")[0]);
        }
        return sourceUri;
    }


    public Set<String> getTargetUris() {
        Set<String>targetUri=new HashSet<>();
        for (String data:targetUris
             ) {
            targetUri.add(data.split("\\?")[0]);
        }
        return targetUri;
    }
}
