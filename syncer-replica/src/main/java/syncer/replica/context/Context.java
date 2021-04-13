package syncer.replica.context;

import syncer.replica.util.tuple.Tuple2;

import java.io.Serializable;

/**
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public interface Context extends Serializable {
    Tuple2<Long, Long> getOffset();
    void setOffset(Tuple2<Long, Long> offset);

    String getReplid();

    long getCurrentOffset();
    
    void setReplid(String replid);

    void setCurrentOffset(long currentOffset);
}
