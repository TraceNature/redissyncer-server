package syncer.transmission.tikv.processor;

import syncer.replica.datatype.command.common.PingCommand;
import syncer.replica.datatype.command.common.SelectCommand;
import syncer.replica.datatype.command.set.SetCommand;
import syncer.replica.event.KeyStringValueSetEvent;
import syncer.replica.event.KeyStringValueStringEvent;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;

/**
 *
 */
public interface IRedis2TikvProcessor {
    /**
     * 初始化
     * @param instId
     * @param tikvUri
     */
    void load(String taskId,String instId,String tikvUri);

    void close();

    /**
     * 全量同步开始事件
     * @param event
     */
    void preRdbSyncEventHandler(PreRdbSyncEvent event);

    /**
     * 全量同步结束事件
     * @param event
     */
    void postRdbSyncEventHandler(PostRdbSyncEvent event);


    /**
     * rdb string 结构
     * @param event
     */
     void rdbStringHandler(KeyStringValueStringEvent event);



    /**
     * rdb set 结构
     * @param event
     */
    void rdbSetHandler(KeyStringValueSetEvent event);

    /**
     * 增量开始
     * @param event
     */
    void preCommandSyncEventHandler(PreCommandSyncEvent event);

    /**
     * 增量结束
     * @param event
     */
    void postCommandSyncEventHandler(PostCommandSyncEvent event);

    /**
     * set 命令
     * @param event
     */
    void setCommandHandler(SetCommand event);

    /**
     * select命令
     * @param event
     */
    void selectCommandHandler(SelectCommand event);


    /**
     * ping
     * @param event
     */
    void pingCommandHandler(PingCommand event);

}
