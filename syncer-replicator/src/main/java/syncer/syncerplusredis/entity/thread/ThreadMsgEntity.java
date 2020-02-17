package syncer.syncerplusredis.entity.thread;

import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.replicator.Replicator;

import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@AllArgsConstructor

@EqualsAndHashCode
public class ThreadMsgEntity implements Serializable {
    private static final long serialVersionUID = -5809782578272943999L;
    private Thread thread;
    private String id;
    private String taskName;
    private ThreadStatusEnum status;
    private RedisClusterDto redisClusterDto;
    private String taskMsg;
    private List<Replicator> rList=new ArrayList<>();
    private Map<String,OffSetEntity>offsetMap=new ConcurrentHashMap<>();
    public ThreadMsgEntity() {
        if(rList==null){
            rList=new ArrayList<>();
        }

        if(offsetMap==null){
            offsetMap=new ConcurrentHashMap<>();
        }
    }

    public synchronized void addReplicator(Replicator r){
        if(rList==null){
            rList=new ArrayList<>();
        }

        if(offsetMap==null){
            offsetMap=new ConcurrentHashMap<>(10);
        }
        if(r!=null) {
            rList.add(r);
        }

    }


    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThreadMsgEntity that = (ThreadMsgEntity) o;
        return Objects.equals(taskName, that.taskName) &&
                status == that.status &&
                Objects.equals(redisClusterDto, that.redisClusterDto) &&
                Objects.equals(taskMsg, that.taskMsg) &&
                Objects.equals(offsetMap, that.offsetMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, status, redisClusterDto, taskMsg, offsetMap);
    }
}
