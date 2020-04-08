package syncer.syncerreplication.event;

import syncer.syncerreplication.util.type.SyncerTuple2;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 命令事件
 * @Date 2020/4/7
 */
public interface Event extends Serializable {
    interface Context extends Serializable {
        /**
         * 命令offset开始和结尾
         * @return
         */
        SyncerTuple2<Long, Long> getOffsets();


        void setOffsets(SyncerTuple2<Long, Long> offset);
    }
}
