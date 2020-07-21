package syncer.syncerreplication.replicator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import syncer.syncerreplication.event.Event;
import syncer.syncerreplication.replicator.ExceptionListener;
import syncer.syncerreplication.replicator.Replicator;

/**
 * @author zhanenqiang
 * @Description 默认异常监听器
 * @Date 2020/4/8
 */
public class DefaultExceptionListener implements ExceptionListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionListener.class);

    @Override
    public void handle(Replicator replicator, Throwable throwable, Event event) {
        logger.error("error on event [{}]", event, throwable);
    }
}
