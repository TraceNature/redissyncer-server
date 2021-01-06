package syncer.replica.event;

import syncer.replica.util.objectutil.type.Tuple2;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 事件体
 * @Date 2020/8/7
 */
public interface Event extends Serializable {

    interface Context extends Serializable {
        Tuple2<Long, Long> getOffsets();
        void setOffsets(Tuple2<Long, Long> offset);
    }
}