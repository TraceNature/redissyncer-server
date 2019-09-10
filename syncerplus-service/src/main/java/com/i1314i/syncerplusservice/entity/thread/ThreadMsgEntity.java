package com.i1314i.syncerplusservice.entity.thread;

import com.i1314i.syncerplusservice.constant.ThreadStatusEnum;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.moilioncircle.redis.replicator.Replicator;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor

@EqualsAndHashCode
public class ThreadMsgEntity implements Serializable {
    private Thread thread;
    private String id;
    private String threadName;
    private ThreadStatusEnum status;
    private RedisClusterDto redisClusterDto;
    private List<Replicator> rList=new ArrayList<>();

    public ThreadMsgEntity() {
        if(rList==null){
            rList=new ArrayList<>();
        }
    }

    public synchronized void addReplicator(Replicator r){
        if(rList==null){
            rList=new ArrayList<>();
        }
        if(r!=null)
            rList.add(r);

    }
}
