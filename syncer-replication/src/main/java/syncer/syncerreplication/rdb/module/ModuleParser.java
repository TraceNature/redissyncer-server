package syncer.syncerreplication.rdb.module;

import syncer.syncerreplication.io.stream.RedisInputStream;
import syncer.syncerreplication.rdb.datatype.Module;

import java.io.IOException;

/**
 * @author zhanenqiang
 * @Description Module类型解析器接口
 * @Date 2020/4/7
 */
public interface ModuleParser<T extends Module> {

    /**
     * @param in      input stream
     * @param version module version : 1 or 2 <p>
     *                {@link syncer.syncerplusredis.Constants#RDB_TYPE_MODULE} : 1 <p>
     *                {@link syncer.syncerplusredis.Constants#RDB_TYPE_MODULE_2} : 2
     * @return module object
     * @throws IOException IOException
     * @since 2.3.0
     */
    T parse(RedisInputStream in, int version) throws IOException;
}