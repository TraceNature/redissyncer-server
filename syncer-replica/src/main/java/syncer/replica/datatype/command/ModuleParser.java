package syncer.replica.datatype.command;

import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.io.RedisInputStream;

import java.io.IOException;


public interface ModuleParser<T extends Module> {

    /**
     * @param in      input stream
     * @param version module version : 1 or 2 <p>
     *                {@link # DB_TYPE_MODULE} : 1 <p>
     *                {@link# RDB_TYPE_MODULE_2} : 2
     * @return module object
     * @throws IOException IOException
     * @since 2.3.0
     */
    T parse(RedisInputStream in, int version) throws IOException;
}
