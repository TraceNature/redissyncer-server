package syncer.replica.listener;

import syncer.replica.event.Event;
import syncer.replica.replication.Replication;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
@Slf4j
public class DefaultExceptionListener implements ExceptionListener {
    @Override
    public void handle(Replication replicator, Throwable throwable, Event event) {
        log.error("error on event [{}]", event, throwable);
    }
}