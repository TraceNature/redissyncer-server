package syncer.syncerplusredis.extend.replicator.visitor;


import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.io.RedisInputStream;
import syncer.syncerplusredis.rdb.BaseRdbParser;
import syncer.syncerplusredis.rdb.RdbValueVisitor;
import syncer.syncerplusredis.rdb.datatype.ContextKeyValuePair;
import syncer.syncerplusredis.rdb.datatype.KeyStringValueString;
import syncer.syncerplusredis.rdb.datatype.KeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;

import java.io.IOException;

import static syncer.syncerplusredis.replicator.Constants.RDB_TYPE_STRING;


/**
 * 不基于dump restore的方式
 */
public class ValueCommandIterableRdbVisitor extends ValueDumpIterableRdbVisitor {


    public ValueCommandIterableRdbVisitor(Replicator replicator) {
        this(replicator, new ValueIterableCommandRdbValueVisitor(replicator));
    }

    public ValueCommandIterableRdbVisitor(Replicator replicator, RdbValueVisitor valueParser) {
        super(replicator, valueParser);
    }

    public ValueCommandIterableRdbVisitor(Replicator replicator, int version) {
        this(replicator, version, 8192);
    }

    public ValueCommandIterableRdbVisitor(Replicator replicator, int version, int size) {
        super(replicator, new ValueIterableCommandRdbValueVisitor(replicator,version,size));
    }


    /**
     * 非dump解析
     * @param in
     * @param version
     * @param context
     * @return
     * @throws IOException
     */

    @Override
    public Event applyString(RedisInputStream in, int version, ContextKeyValuePair context) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        KeyValuePair<byte[], byte[]> o0 = new KeyStringValueString();
        byte[] key = parser.rdbLoadEncodedStringObject().first();

        byte[] val = valueVisitor.applyString(in, version);
        o0.setValueRdbType(RDB_TYPE_STRING);
        o0.setValue(val);
        o0.setKey(key);
        return context.valueOf(o0);
    }

}