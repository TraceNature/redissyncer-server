package syncer.replica.parser.syncer;

import static syncer.replica.constant.Constants.MODULE_SET;
import static syncer.replica.constant.Constants.RDB_LOAD_NONE;
import static syncer.replica.constant.Constants.RDB_MODULE_OPCODE_EOF;
import static syncer.replica.constant.Constants.RDB_TYPE_HASH;
import static syncer.replica.constant.Constants.RDB_TYPE_HASH_ZIPLIST;
import static syncer.replica.constant.Constants.RDB_TYPE_HASH_ZIPMAP;
import static syncer.replica.constant.Constants.RDB_TYPE_LIST;
import static syncer.replica.constant.Constants.RDB_TYPE_LIST_QUICKLIST;
import static syncer.replica.constant.Constants.RDB_TYPE_LIST_ZIPLIST;
import static syncer.replica.constant.Constants.RDB_TYPE_MODULE;
import static syncer.replica.constant.Constants.RDB_TYPE_MODULE_2;
import static syncer.replica.constant.Constants.RDB_TYPE_SET;
import static syncer.replica.constant.Constants.RDB_TYPE_SET_INTSET;
import static syncer.replica.constant.Constants.RDB_TYPE_STREAM_LISTPACKS;
import static syncer.replica.constant.Constants.RDB_TYPE_STRING;
import static syncer.replica.constant.Constants.RDB_TYPE_ZSET;
import static syncer.replica.constant.Constants.RDB_TYPE_ZSET_2;
import static syncer.replica.constant.Constants.RDB_TYPE_ZSET_ZIPLIST;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.io.ByteBufferOutputStream;
import syncer.replica.io.RedisInputStream;
import syncer.replica.listener.TaskRawByteListener;
import syncer.replica.parser.BaseRdbEncoder;
import syncer.replica.parser.BaseRdbParser;
import syncer.replica.parser.DefaultRedisRdbValueParser;
import syncer.replica.parser.skip.SkipRdbParser;
import syncer.replica.replication.Replication;
import syncer.replica.util.CRC64;
import syncer.replica.util.bytes.ByteArray;
import syncer.replica.util.bytes.ByteBuilder;


/**
 * dump value
 */
public class DumpRdbValueParser extends DefaultRedisRdbValueParser {
    private final int size;
    private final int version;

    public DumpRdbValueParser(Replication replication) {
        this(replication, -1);
    }

    public DumpRdbValueParser(Replication replication, int version) {
        this(replication, version, 8192);
    }

    public DumpRdbValueParser(Replication replication, int version, int size) {
        super(replication);
        this.version = version;
        this.size = size;
    }
    
    @Override
    public <T> T parseString(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_STRING, version);
        replication.addRawByteListener(listener);
        try {
            new SkipRdbParser(in).rdbLoadEncodedStringObject();
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseList(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_LIST, version);
        replication.addRawByteListener(listener);
        try {
            SkipRdbParser skipParser = new SkipRdbParser(in);
            long len = skipParser.rdbLoadLen().len;
            while (len > 0) {
                skipParser.rdbLoadEncodedStringObject();
                len--;
            }
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseSet(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_SET, version);
        replication.addRawByteListener(listener);
        try {
            SkipRdbParser skipParser = new SkipRdbParser(in);
            long len = skipParser.rdbLoadLen().len;
            while (len > 0) {
                skipParser.rdbLoadEncodedStringObject();
                len--;
            }
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseZSet(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_ZSET, version);
        replication.addRawByteListener(listener);
        try {
            SkipRdbParser skipParser = new SkipRdbParser(in);
            long len = skipParser.rdbLoadLen().len;
            while (len > 0) {
                skipParser.rdbLoadEncodedStringObject();
                skipParser.rdbLoadDoubleValue();
                len--;
            }
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseZSet2(RedisInputStream in, int version) throws IOException {
        if (this.version != -1 && this.version < 8 /* since redis rdb version 8 */) {
            // downgrade to RDB_TYPE_ZSET
            BaseRdbParser parser = new BaseRdbParser(in);
            BaseRdbEncoder encoder = new BaseRdbEncoder();
            DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_ZSET, version);
            try (ByteBufferOutputStream out = new ByteBufferOutputStream(size)) {
                long len = parser.rdbLoadLen().len;
                listener.handler(encoder.rdbSaveLen(len));
                while (len > 0) {
                    ByteArray element = parser.rdbLoadEncodedStringObject();
                    encoder.rdbGenericSaveStringObject(element, out);
                    double score = parser.rdbLoadBinaryDoubleValue();
                    encoder.rdbSaveDoubleValue(score, out);
                    len--;
                }
                listener.handle(out.toByteBuffer());
                return (T) listener.getBytes();
            }
        } else {
            DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_ZSET_2, version);
            replication.addRawByteListener(listener);
            try {
                SkipRdbParser skipParser = new SkipRdbParser(in);
                long len = skipParser.rdbLoadLen().len;
                while (len > 0) {
                    skipParser.rdbLoadEncodedStringObject();
                    skipParser.rdbLoadBinaryDoubleValue();
                    len--;
                }
            } finally {
                replication.removeRawByteListener(listener);
            }
            return (T) listener.getBytes();
        }
    }

    @Override
    public <T> T parseHash(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_HASH, version);
        replication.addRawByteListener(listener);
        try {
            SkipRdbParser skipParser = new SkipRdbParser(in);
            long len = skipParser.rdbLoadLen().len;
            while (len > 0) {
                skipParser.rdbLoadEncodedStringObject();
                skipParser.rdbLoadEncodedStringObject();
                len--;
            }
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseHashZipMap(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_HASH_ZIPMAP, version);
        replication.addRawByteListener(listener);
        try {
            new SkipRdbParser(in).rdbLoadPlainStringObject();
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseListZipList(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_LIST_ZIPLIST, version);
        replication.addRawByteListener(listener);
        try {
            new SkipRdbParser(in).rdbLoadPlainStringObject();
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseSetIntSet(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_SET_INTSET, version);
        replication.addRawByteListener(listener);
        try {
            new SkipRdbParser(in).rdbLoadPlainStringObject();
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseZSetZipList(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_ZSET_ZIPLIST, version);
        replication.addRawByteListener(listener);
        try {
            new SkipRdbParser(in).rdbLoadPlainStringObject();
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseHashZipList(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_HASH_ZIPLIST, version);
        replication.addRawByteListener(listener);
        try {
            new SkipRdbParser(in).rdbLoadPlainStringObject();
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseListQuickList(RedisInputStream in, int version) throws IOException {
        if (this.version != -1 && this.version < 7 /* since redis rdb version 7 */) {
            // downgrade to RDB_TYPE_LIST
            BaseRdbParser parser = new BaseRdbParser(in);
            BaseRdbEncoder encoder = new BaseRdbEncoder();
            try (ByteBufferOutputStream out = new ByteBufferOutputStream(size)) {
                int total = 0;
                long len = parser.rdbLoadLen().len;
                for (long i = 0; i < len; i++) {
                    RedisInputStream stream = new RedisInputStream(parser.rdbGenericLoadStringObject(RDB_LOAD_NONE));
            
                    BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
                    BaseRdbParser.LenHelper.zltail(stream); // zltail
                    int zllen = BaseRdbParser.LenHelper.zllen(stream);
                    for (int j = 0; j < zllen; j++) {
                        byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
                        encoder.rdbGenericSaveStringObject(new ByteArray(e), out);
                        total++;
                    }
                    int zlend = BaseRdbParser.LenHelper.zlend(stream);
                    if (zlend != 255) {
                        throw new AssertionError("zlend expect 255 but " + zlend);
                    }
                }
        
                DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_LIST, version);
                listener.handler(encoder.rdbSaveLen(total));
                listener.handle(out.toByteBuffer());
                return (T) listener.getBytes();
            }
        } else {
            DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_LIST_QUICKLIST, version);
            replication.addRawByteListener(listener);
            try {
                SkipRdbParser skipParser = new SkipRdbParser(in);
                long len = skipParser.rdbLoadLen().len;
                for (long i = 0; i < len; i++) {
                    skipParser.rdbGenericLoadStringObject();
                }
            } finally {
                replication.removeRawByteListener(listener);
            }
            return (T) listener.getBytes();
        }
    }

    @Override
    public <T> T parseModule(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_MODULE, version);
        replication.addRawByteListener(listener);
        try {
            SkipRdbParser skipParser = new SkipRdbParser(in);
            char[] c = new char[9];
            long moduleid = skipParser.rdbLoadLen().len;
            for (int i = 0; i < c.length; i++) {
                c[i] = MODULE_SET[(int) (moduleid >>> (10 + (c.length - 1 - i) * 6) & 63)];
            }
            String moduleName = new String(c);
            int moduleVersion = (int) (moduleid & 1023);
            ModuleParser<? extends Module> moduleParser = lookupModuleParser(moduleName, moduleVersion);
            if (moduleParser == null) {
                throw new NoSuchElementException("module parser[" + moduleName + ", " + moduleVersion + "] not register. rdb type: [RDB_TYPE_MODULE]");
            }
            moduleParser.parse(in, 1);
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseModule2(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_MODULE_2, version);
        replication.addRawByteListener(listener);
        try {
            BaseRdbParser parser = new BaseRdbParser(in);
            SkipRdbParser skipParser = new SkipRdbParser(in);
            char[] c = new char[9];
            long moduleid = skipParser.rdbLoadLen().len;
            for (int i = 0; i < c.length; i++) {
                c[i] = MODULE_SET[(int) (moduleid >>> (10 + (c.length - 1 - i) * 6) & 63)];
            }
            String moduleName = new String(c);
            int moduleVersion = (int) (moduleid & 1023);
            ModuleParser<? extends Module> moduleParser = lookupModuleParser(moduleName, moduleVersion);
            if (moduleParser == null) {
                SkipRdbParser skipRdbParser = new SkipRdbParser(in);
                skipRdbParser.rdbLoadCheckModuleValue();
            } else {
                moduleParser.parse(in, 2);
                long eof = parser.rdbLoadLen().len;
                if (eof != RDB_MODULE_OPCODE_EOF) {
                    throw new UnsupportedOperationException("The RDB file contains module data for the module '" + moduleName + "' that is not terminated by the proper module value EOF marker");
                }
            }
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }

    @Override
    @SuppressWarnings("resource")
    public <T> T parseStreamListPacks(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_STREAM_LISTPACKS, version);
        replication.addRawByteListener(listener);
        try {
            SkipRdbParser skipParser = new SkipRdbParser(in);
            long listPacks = skipParser.rdbLoadLen().len;
            while (listPacks-- > 0) {
                skipParser.rdbLoadPlainStringObject();
                skipParser.rdbLoadPlainStringObject();
            }
            skipParser.rdbLoadLen();
            skipParser.rdbLoadLen();
            skipParser.rdbLoadLen();
            long groupCount = skipParser.rdbLoadLen().len;
            while (groupCount-- > 0) {
                skipParser.rdbLoadPlainStringObject();
                skipParser.rdbLoadLen();
                skipParser.rdbLoadLen();
                long groupPel = skipParser.rdbLoadLen().len;
                while (groupPel-- > 0) {
                    in.skip(16);
                    skipParser.rdbLoadMillisecondTime();
                    skipParser.rdbLoadLen();
                }
                long consumerCount = skipParser.rdbLoadLen().len;
                while (consumerCount-- > 0) {
                    skipParser.rdbLoadPlainStringObject();
                    skipParser.rdbLoadMillisecondTime();
                    long consumerPel = skipParser.rdbLoadLen().len;
                    while (consumerPel-- > 0) {
                        in.skip(16);
                    }
                }
            }
        } finally {
            replication.removeRawByteListener(listener);
        }
        return (T) listener.getBytes();
    }
    
    
    private class DefaultRawByteListener implements TaskRawByteListener {
        private final int version;
        private final ByteBuilder builder;

        private DefaultRawByteListener(byte type, int version) {
            this.builder = ByteBuilder.allocate(DumpRdbValueParser.this.size);
            this.builder.put(type);
            int ver = DumpRdbValueParser.this.version;
            this.version = ver == -1 ? version : ver;
        }

        @Override
        public void handler(byte... rawBytes) {
            for (byte b : rawBytes) {
                this.builder.put(b);
            }
        }
        
        public void handle(ByteBuffer buffer) {
            this.builder.put(buffer);
        }

        public byte[] getBytes() {
            this.builder.put((byte) version);
            this.builder.put((byte) 0x00);
            byte[] bytes = this.builder.array();
            byte[] crc = CRC64.longToByteArray(CRC64.crc64(bytes));
            for (byte b : crc) {
                this.builder.put(b);
            }
            return this.builder.array();
        }

    }
}
