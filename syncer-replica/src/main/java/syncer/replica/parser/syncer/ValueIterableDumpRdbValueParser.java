package syncer.replica.parser.syncer;



import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.io.RedisInputStream;
import syncer.replica.listener.TaskRawByteListener;
import syncer.replica.parser.BaseRdbParser;
import syncer.replica.parser.DefaultRedisRdbValueParser;
import syncer.replica.parser.IRdbValueParser;
import syncer.replica.parser.skip.SkipRdbParser;
import syncer.replica.replication.Replication;
import syncer.replica.util.CRC64;
import syncer.replica.util.bytes.ByteBuilder;
import syncer.replica.util.strings.Strings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import static syncer.replica.constant.Constants.*;

public class ValueIterableDumpRdbValueParser extends DefaultRedisRdbValueParser implements IRdbValueParser {
    private final int size;
    private final int version;

    public ValueIterableDumpRdbValueParser(Replication replication) {
        this(replication, -1);
    }

    public ValueIterableDumpRdbValueParser(Replication replication, int version) {
        this(replication, version, 8192);
    }


    public ValueIterableDumpRdbValueParser(Replication replication, int version, int size) {
        super(replication);
        this.version = version;
        this.size = size;
    }

    private class DefaultRawByteListener implements TaskRawByteListener {
        private final int version;
        private final ByteBuilder builder;

        private DefaultRawByteListener(byte type, int version) {
            this.builder = ByteBuilder.allocate(ValueIterableDumpRdbValueParser.this.size);
            this.builder.put(type);
            int ver = ValueIterableDumpRdbValueParser.this.version;
            this.version = ver == -1 ? version : ver;
        }

        @Override
        public void handler(byte... rawBytes) {
            for (byte b : rawBytes) {
                this.builder.put(b);
            }
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


    /**
     * 迭代
     *
     * @param in
     * @param version
     * @param <T>
     * @return
     * @throws IOException
     */
//    @Override
//    public <T> T parseList1(RedisInputStream in, int version) throws IOException {
//        /*
//         * |    <len>     |       <content>       |
//         * | 1 or 5 bytes |    string contents    |
//         */
//        BaseRdbParser parser = new BaseRdbParser(in);
//
//        long len = parser.rdbLoadLen().len;
//        Iterator<byte[]> val = new Iter<byte[]>(len, parser) {
//            @Override
//            public boolean hasNext() {
//                return condition > 0;
//            }
//
//            @Override
//            public byte[] next() {
//                try {
//                    byte[] element = parser.rdbLoadEncodedStringObject().first();
//                    condition--;
//                    return element;
//                } catch (IOException e) {
//                    throw new UncheckedIOException(e);
//                }
//            }
//        };
//        return (T) val;
//    }
    @Override
    public <T> T parseList(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |
         * | 1 or 5 bytes |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        Iterator<byte[]> val = new Iter<byte[]>(len, parser) {
            @Override
            public boolean hasNext() {
                return condition > 0;
            }
            @Override
            public byte[] next() {
                try {
                    byte[] element = parser.rdbLoadEncodedStringObject().first();
                    condition--;
                    return element;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }


    @Override
    public <T> T parseSet(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |
         * | 1 or 5 bytes |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        Iterator<byte[]> val = new Iter<byte[]>(len, parser) {
            @Override
            public boolean hasNext() {
                return condition > 0;
            }
            @Override
            public byte[] next() {
                try {
                    byte[] element = parser.rdbLoadEncodedStringObject().first();
                    condition--;
                    return element;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseZSet(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |        <score>       |
         * | 1 or 5 bytes |    string contents    |    double content    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        Iterator<ZSetEntry> val = new Iter<ZSetEntry>(len, parser) {
            @Override
            public boolean hasNext() {
                return condition > 0;
            }
            @Override
            public ZSetEntry next() {
                try {
                    byte[] element = parser.rdbLoadEncodedStringObject().first();
                    double score = parser.rdbLoadDoubleValue();
                    condition--;
                    return new ZSetEntry(element, score);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseZSet2(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |        <score>       |
         * | 1 or 5 bytes |    string contents    |    binary double     |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        /* rdb version 8*/
        long len = parser.rdbLoadLen().len;
        Iterator<ZSetEntry> val = new Iter<ZSetEntry>(len, parser) {
            @Override
            public boolean hasNext() {
                return condition > 0;
            }
            @Override
            public ZSetEntry next() {
                try {
                    byte[] element = parser.rdbLoadEncodedStringObject().first();
                    double score = parser.rdbLoadBinaryDoubleValue();
                    condition--;
                    return new ZSetEntry(element, score);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseHash(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |
         * | 1 or 5 bytes |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        Iterator<Map.Entry<byte[], byte[]>> val = new Iter<Map.Entry<byte[], byte[]>>(len, parser) {
            @Override
            public boolean hasNext() {
                return condition > 0;
            }
            @Override
            public Map.Entry<byte[], byte[]> next() {
                try {
                    byte[] field = parser.rdbLoadEncodedStringObject().first();
                    byte[] value = parser.rdbLoadEncodedStringObject().first();
                    condition--;
                    return new AbstractMap.SimpleEntry<>(field, value);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseHashZipMap(RedisInputStream in, int version) throws IOException {
        /*
         * |<zmlen> |   <len>     |"foo"    |    <len>   | <free> |   "bar" |<zmend> |
         * | 1 byte | 1 or 5 byte | content |1 or 5 byte | 1 byte | content | 1 byte |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zmlen(stream); // zmlen
        Iterator<Map.Entry<byte[], byte[]>> val = new HashZipMapIter(stream);
        return (T) val;
    }

    @Override
    public <T> T parseListZipList(RedisInputStream in, int version) throws IOException {
        /*
         * |<zlbytes>| <zltail>| <zllen>| <entry> ...<entry> | <zlend>|
         * | 4 bytes | 4 bytes | 2bytes | zipListEntry ...   | 1byte  |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        final RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        Iterator<byte[]> val = new Iter<byte[]>(zllen, null) {
            @Override
            public boolean hasNext() {
                if (condition > 0) {
                    return true;
                }
                try {
                    int zlend = BaseRdbParser.LenHelper.zlend(stream);
                    if (zlend != 255) {
                        throw new AssertionError("zlend expect 255 but " + zlend);
                    }
                    return false;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public byte[] next() {
                try {
                    byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
                    condition--;
                    return e;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseSetIntSet(RedisInputStream in, int version) throws IOException {
        /*
         * |<encoding>| <length-of-contents>|              <contents>                            |
         * | 4 bytes  |            4 bytes  | 2 bytes element| 4 bytes element | 8 bytes element |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        final RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        final int encoding = BaseRdbParser.LenHelper.encoding(stream);
        long lenOfContent = BaseRdbParser.LenHelper.lenOfContent(stream);
        Iterator<byte[]> val = new Iter<byte[]>(lenOfContent, null) {
            @Override
            public boolean hasNext() {
                return condition > 0;
            }
            @Override
            public byte[] next() {
                try {
                    switch (encoding) {
                        case 2:
                            String element = String.valueOf(stream.readInt(2));
                            condition--;
                            return element.getBytes();
                        case 4:
                            element = String.valueOf(stream.readInt(4));
                            condition--;
                            return element.getBytes();
                        case 8:
                            element = String.valueOf(stream.readLong(8));
                            condition--;
                            return element.getBytes();
                        default:
                            throw new AssertionError("expect encoding [2,4,8] but:" + encoding);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseZSetZipList(RedisInputStream in, int version) throws IOException {
        /*
         * |<zlbytes>| <zltail>| <zllen>| <entry> ...<entry> | <zlend>|
         * | 4 bytes | 4 bytes | 2bytes | zipListEntry ...   | 1byte  |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        final RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        Iterator<ZSetEntry> val = new Iter<ZSetEntry>(zllen, null) {
            @Override
            public boolean hasNext() {
                if (condition > 0) {
                    return true;
                }
                try {
                    int zlend = BaseRdbParser.LenHelper.zlend(stream);
                    if (zlend != 255) {
                        throw new AssertionError("zlend expect 255 but " + zlend);
                    }
                    return false;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public ZSetEntry next() {
                try {
                    byte[] element = BaseRdbParser.StringHelper.zipListEntry(stream);
                    condition--;
                    double score = Double.valueOf(Strings.toString(BaseRdbParser.StringHelper.zipListEntry(stream)));
                    condition--;
                    return new ZSetEntry(element, score);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseHashZipList(RedisInputStream in, int version) throws IOException {
        /*
         * |<zlbytes>| <zltail>| <zllen>| <entry> ...<entry> | <zlend>|
         * | 4 bytes | 4 bytes | 2bytes | zipListEntry ...   | 1byte  |
         */
        BaseRdbParser parser = new BaseRdbParser(in);
        final RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        Iterator<Map.Entry<byte[], byte[]>> val = new Iter<Map.Entry<byte[], byte[]>>(zllen, null) {
            @Override
            public boolean hasNext() {
                if (condition > 0) {
                    return true;
                }
                try {
                    int zlend = BaseRdbParser.LenHelper.zlend(stream);
                    if (zlend != 255) {
                        throw new AssertionError("zlend expect 255 but " + zlend);
                    }
                    return false;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public Map.Entry<byte[], byte[]> next() {
                try {
                    byte[] field = BaseRdbParser.StringHelper.zipListEntry(stream);
                    condition--;
                    byte[] value = BaseRdbParser.StringHelper.zipListEntry(stream);
                    condition--;
                    return new AbstractMap.SimpleEntry<>(field, value);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        return (T) val;
    }

    @Override
    public <T> T parseListQuickList(RedisInputStream in, int version) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);
        long len = parser.rdbLoadLen().len;
        Iterator<byte[]> val = new QuickListIter(len, parser);
        return (T) val;
    }

    private static abstract class Iter<T> implements Iterator<T> {

        protected long condition;
        protected final BaseRdbParser parser;

        private Iter(long condition, BaseRdbParser parser) {
            this.condition = condition;
            this.parser = parser;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class HashZipMapIter extends Iter<Map.Entry<byte[], byte[]>> {

        protected int zmEleLen;
        protected final RedisInputStream stream;
        private HashZipMapIter(RedisInputStream stream) {
            super(0, null);
            this.stream = stream;
        }

        @Override
        public boolean hasNext() {
            try {
                return (this.zmEleLen = BaseRdbParser.LenHelper.zmElementLen(stream)) != 255;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Map.Entry<byte[], byte[]> next() {
            try {
                byte[] field = BaseRdbParser.StringHelper.bytes(stream, zmEleLen);
                this.zmEleLen = BaseRdbParser.LenHelper.zmElementLen(stream);
                if (this.zmEleLen == 255) {
                    return new AbstractMap.SimpleEntry<>(field, null);
                }
                int free = BaseRdbParser.LenHelper.free(stream);
                byte[] value = BaseRdbParser.StringHelper.bytes(stream, zmEleLen);
                BaseRdbParser.StringHelper.skip(stream, free);
                return new AbstractMap.SimpleEntry<>(field, value);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class QuickListIter extends Iter<byte[]> {
        protected int zllen = -1;
        protected RedisInputStream stream;

        private QuickListIter(long condition, BaseRdbParser parser) {
            super(condition, parser);
        }
        @Override
        public boolean hasNext() {
            return zllen > 0 || condition > 0;
        }
        @Override
        public byte[] next() {
            try {
                if (zllen == -1 && condition > 0) {
                    this.stream = new RedisInputStream(parser.rdbGenericLoadStringObject(RDB_LOAD_NONE));
                    BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
                    BaseRdbParser.LenHelper.zltail(stream); // zltail
                    this.zllen = BaseRdbParser.LenHelper.zllen(stream);
                    if (zllen == 0) {
                        int zlend = BaseRdbParser.LenHelper.zlend(stream);
                        if (zlend != 255) {
                            throw new AssertionError("zlend expect 255 but " + zlend);
                        }
                        zllen = -1;
                        condition--;
                    }
                    if (hasNext()) {
                        return next();
                    }
                    throw new IllegalStateException("end of iterator");
                } else {
                    byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
                    zllen--;
                    if (zllen == 0) {
                        int zlend = BaseRdbParser.LenHelper.zlend(stream);
                        if (zlend != 255) {
                            throw new AssertionError("zlend expect 255 but " + zlend);
                        }
                        zllen = -1;
                        condition--;
                    }
                    return e;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


    @Override
    public <T> T parseString(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_STRING, version);
        replication.addRawByteListener(listener);
        new SkipRdbParser(in).rdbLoadEncodedStringObject();
        replication.removeRawByteListener(listener);
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseModule(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_MODULE, version);
        replication.addRawByteListener(listener);
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
        replication.removeRawByteListener(listener);
        return (T) listener.getBytes();
    }

    @Override
    public <T> T parseModule2(RedisInputStream in, int version) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);

        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_MODULE_2, version);
        replication.addRawByteListener(listener);
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
        replication.removeRawByteListener(listener);
        return (T) listener.getBytes();
    }

    @Override
    @SuppressWarnings("resource")
    public <T> T parseStreamListPacks(RedisInputStream in, int version) throws IOException {
        DefaultRawByteListener listener = new DefaultRawByteListener((byte) RDB_TYPE_STREAM_LISTPACKS, version);
        replication.addRawByteListener(listener);
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
        replication.removeRawByteListener(listener);
        return (T) listener.getBytes();
    }
}
