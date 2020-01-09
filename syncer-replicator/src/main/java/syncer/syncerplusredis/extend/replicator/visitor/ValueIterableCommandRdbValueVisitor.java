package syncer.syncerplusredis.extend.replicator.visitor;


import syncer.syncerplusredis.io.RedisInputStream;
import syncer.syncerplusredis.rdb.BaseRdbParser;
import syncer.syncerplusredis.replicator.Replicator;

import java.io.IOException;


public class ValueIterableCommandRdbValueVisitor extends ValueIterableDumpRdbValueVisitor {

    public ValueIterableCommandRdbValueVisitor(Replicator replicator) {
        super(replicator);
    }

    public ValueIterableCommandRdbValueVisitor(Replicator replicator, int version) {
        super(replicator, version);
    }

    public ValueIterableCommandRdbValueVisitor(Replicator replicator, int version, int size) {
        super(replicator, version, size);
    }

    /**
     * 非dump解析
     * @param in
     * @param version
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T applyString(RedisInputStream in, int version) throws IOException {
        /*
         * |       <content>       |
         * |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        byte[] val = parser.rdbLoadEncodedStringObject().first();
        return (T) val;
    }
}