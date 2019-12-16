package syncer.syncerservice.util;

import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.ObjectUtils;

public class KVUtils {
    public static synchronized String getKey(Event event) {
        try {
            if (event instanceof DefaultCommand) {
                DefaultCommand dc = (DefaultCommand) event;
                return Strings.byteToString(dc.getArgs()[0]);
            }else   if (event instanceof BatchedKeyValuePair<?, ?>) {
                BatchedKeyValuePair batchedKeyValuePair = (BatchedKeyValuePair) event;
                return (String) batchedKeyValuePair.getKey();
            }else   if (event instanceof DumpKeyValuePair) {
                DumpKeyValuePair valueDump = (DumpKeyValuePair) event;

                return Strings.byteToString(valueDump.getKey());
            }

        }catch (Exception e){

        }
        return null;
    }
}
