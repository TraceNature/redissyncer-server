package syncer.transmission.task;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/29
 */
public class ContextTaskStatus {
    public static volatile AtomicBoolean STATUS=new AtomicBoolean(false);
}
