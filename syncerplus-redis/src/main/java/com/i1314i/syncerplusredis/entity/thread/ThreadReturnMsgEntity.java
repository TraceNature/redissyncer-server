package com.i1314i.syncerplusredis.entity.thread;



import com.i1314i.syncerplusredis.constant.ThreadStatusEnum;
import com.i1314i.syncerplusredis.entity.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

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
    @Builder.Default
    private String taskMsg="";
    private boolean afresh;
    private Map<Integer,Integer> dbNum;
    @Builder.Default
    private Set<String> sourceUris=new HashSet<>();
    @Builder.Default
    private Set<String>targetUris=new HashSet<>();
    private double targetRedisVersion;

    @Builder.Default
    private FileType fileType=FileType.SYNC;

    public Set<String> getSourceUris() {
        Set<String>sourceUri=new HashSet<>();
        for (String data:sourceUris
        ) {
            if(!StringUtils.isEmpty(data))
              sourceUri.add(data.split("\\?")[0]);
        }
        return sourceUri;
    }


    public Set<String> getTargetUris() {
        Set<String>targetUri=new HashSet<>();
        for (String data:targetUris
             ) {
            if(!StringUtils.isEmpty(data))
             targetUri.add(data.split("\\?")[0]);
        }
        return targetUri;
    }

    public void loading(){
        if(sourceUris==null){
            sourceUris=new HashSet<>();
        }

        if(targetUris==null){
            targetUris=new HashSet<>();
        }
    }
}
