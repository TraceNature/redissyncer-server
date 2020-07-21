package syncer.syncerservice.MultiMasterReplication.sync;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/28
 */
public class SysncData {
    public static Map<String, AtomicLong> dataMap=new ConcurrentHashMap<>();
    public static  Map<String, AtomicLong>dataMapB=new ConcurrentHashMap<>();
}
