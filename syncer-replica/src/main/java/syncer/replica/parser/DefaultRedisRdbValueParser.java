package syncer.replica.parser;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.constant.Constants;
import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.datatype.rdb.stream.*;
import syncer.replica.datatype.rdb.zset.ZSetEntry;
import syncer.replica.io.RedisInputStream;
import syncer.replica.parser.skip.SkipRdbParser;
import syncer.replica.replication.Replication;
import syncer.replica.util.list.ByteArrayList;
import syncer.replica.util.map.ByteArrayMap;
import syncer.replica.util.set.ByteArraySet;
import syncer.replica.util.strings.Strings;

import java.io.IOException;
import java.util.*;

import static syncer.replica.parser.BaseRdbParser.StringHelper.listPackEntry;

/**
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/

@Slf4j
public  class DefaultRedisRdbValueParser implements IRdbValueParser {

    protected final Replication replication;

    public DefaultRedisRdbValueParser(final Replication replication) {
        this.replication = replication;
    }

    @Override
    public <T> T parseString(RedisInputStream in, int version) throws IOException {
        /*
         * |       <content>       |
         * |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        byte[] val = parser.rdbLoadEncodedStringObject().first();
        return (T) val;
    }

    /**
     * A redis list is represented as a sequence of strings.
     *
     * First, the size of the list size is read from the stream using "Length Encoding"
     * Next, size strings are read from the stream using "String Encoding"
     * The list is then re-constructed using these Strings
     * @param in
     * @param version
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T parseList(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |
         * | 1 or 5 bytes |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        long len = parser.rdbLoadLen().len;
        List<byte[]> list = new ByteArrayList();
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            list.add(element);
            len--;
        }
        return (T) list;
    }

    @Override
    public <T> T parseSet(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |
         * | 1 or 5 bytes |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        long len = parser.rdbLoadLen().len;
        Set<byte[]> set = new ByteArraySet();
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            set.add(element);
            len--;
        }
        return (T) set;
    }

    @Override
    public <T> T parseZSet(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |        <score>       |
         * | 1 or 5 bytes |    string contents    |    double content    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        long len = parser.rdbLoadLen().len;
        Set<ZSetEntry> zset = new LinkedHashSet<>();
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            double score = parser.rdbLoadDoubleValue();
            zset.add(new ZSetEntry(element, score));
            len--;
        }
        return (T) zset;
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
        Set<ZSetEntry> zset = new LinkedHashSet<>();
        while (len > 0) {
            byte[] element = parser.rdbLoadEncodedStringObject().first();
            double score = parser.rdbLoadBinaryDoubleValue();
            zset.add(new ZSetEntry(element, score));
            len--;
        }
        return (T) zset;
    }

    /**
     * First, the size of the hash size is read from the stream using "Length Encoding"
     * Next, 2 * size strings are read from the stream using "String Encoding"
     * Alternate strings are key and values
     * For example, 2 us washington india delhi represents the map {"us" => "washington", "india" => "delhi"}
     * @param in
     * @param version
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T parseHash(RedisInputStream in, int version) throws IOException {
        /*
         * |    <len>     |       <content>       |
         * | 1 or 5 bytes |    string contents    |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        long len = parser.rdbLoadLen().len;
        ByteArrayMap map = new ByteArrayMap();
        while (len > 0) {
            byte[] field = parser.rdbLoadEncodedStringObject().first();
            byte[] value = parser.rdbLoadEncodedStringObject().first();
            map.put(field, value);
            len--;
        }
        return (T) map;
    }

    /**
     * NOTE : Zipmap encoding are deprecated starting Redis 2.6. Small hashmaps are now encoded using ziplists.
     *
     * A Zipmap is a hashmap that has been serialized to a string. In essence, the key value pairs are stored sequentially. Looking up a key in this structure is O(N). This structure is used instead of a dictionary when the number of key value pairs are small.
     *
     * To parse a zipmap, first a string is read from the stream using "String Encoding". This string is the envelope of the zipmap. The contents of this string represent the zipmap.
     *
     * The structure of a zipmap within this string is as follows -
     *
     * <zmlen><len>"foo"<len><free>"bar"<len>"hello"<len><free>"world"<zmend>
     *
     * zmlen : Is a 1 byte length that holds the size of the zip map. If it is greater than or equal to 254, value is not used. You will have to iterate the entire zip map to find the length.
     * len : Is the length of the following string, which can be either a key or a value. This length is stored in either 1 byte or 5 bytes (yes, it differs from "Length Encoding" described above). If the first byte is between 0 and 252, that is the length of the zipmap. If the first byte is 253, then the next 4 bytes read as an unsigned integer represent the length of the zipmap. 254 and 255 are invalid values for this field.
     * free : This is always 1 byte, and indicates the number of free bytes after the value. For example, if the value of a key is "America" and its get updated to "USA", 4 free bytes will be available.
     * zmend : Always 255. Indicates the end of the zipmap.
     * Worked Example
     *
     * 18 02 06 4d 4b 44 31 47 36 01 00 32 05 59 4e 4e 58 4b 04 00 46 37 54 49 ff ..
     *
     * Start by decoding this using "String Encoding". You will notice that 18 is the length of the string. Accordingly, we will read the next 24 bytes i.e. upto ff
     * Now, we are parsing the string starting at 02 06... using the "Zipmap Encoding"
     * 02 is the number of entries in the hashmap.
     * 06 is the length of the next string. Since this is less than 254, we don't have to read any additional bytes
     * We read the next 6 bytes i.e. 4d 4b 44 31 47 36 to get the key "MKD1G6"
     * 01 is the length of the next string, which would be the value
     * 00 is the number of free bytes
     * We read the next 1 byte(s), which is 0x32. Thus, we get our value "2"
     * In this case, the free bytes is 0, so we don't skip anything
     * 05 is the length of the next string, in this case a key.
     * We read the next 5 bytes 59 4e 4e 58 4b, to get the key "YNNXK"
     * 04 is the length of the next string, which is a value
     * 00 is the number of free bytes after the value
     * We read the next 4 bytes i.e. 46 37 54 49 to get the value "F7TI"
     * Finally, we encounter FF, which indicates the end of this zip map
     * Thus, this zip map represents the hash {"MKD1G6" => "2", "YNNXK" => "F7TI"}
     * @param in
     * @param version
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T parseHashZipMap(RedisInputStream in, int version) throws IOException {
        /*
         * |<zmlen> |   <len>     |"foo"    |    <len>   | <free> |   "bar" |<zmend> |
         * | 1 byte | 1 or 5 byte | content |1 or 5 byte | 1 byte | content | 1 byte |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        ByteArrayMap map = new ByteArrayMap();
        // zmlen
        BaseRdbParser.LenHelper.zmlen(stream);
        while (true) {
            int zmEleLen = BaseRdbParser.LenHelper.zmElementLen(stream);
            if (zmEleLen == 255) {
                return (T) map;
            }
            byte[] field = BaseRdbParser.StringHelper.bytes(stream, zmEleLen);
            zmEleLen = BaseRdbParser.LenHelper.zmElementLen(stream);
            if (zmEleLen == 255) {
                //value is null
                map.put(field, null);
                return (T) map;
            }
            int free = BaseRdbParser.LenHelper.free(stream);
            byte[] value = BaseRdbParser.StringHelper.bytes(stream, zmEleLen);
            BaseRdbParser.StringHelper.skip(stream, free);
            map.put(field, value);
        }
    }

    @Override
    public <T> T parseListZipList(RedisInputStream in, int version) throws IOException {
        /*
         * |<zlbytes>| <zltail>| <zllen>| <entry> ...<entry> | <zlend>|
         * | 4 bytes | 4 bytes | 2bytes | zipListEntry ...   | 1byte  |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        List<byte[]> list = new ByteArrayList();
        // zlbytes
        BaseRdbParser.LenHelper.zlbytes(stream);
        // zltail
        BaseRdbParser.LenHelper.zltail(stream);
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        for (int i = 0; i < zllen; i++) {
            byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
            list.add(e);
        }
        int zlend = BaseRdbParser.LenHelper.zlend(stream);
        if (zlend != 255) {
            throw new AssertionError("zlend expect 255 but " + zlend);
        }
        return (T) list;
    }

    @Override
    public <T> T parseSetIntSet(RedisInputStream in, int version) throws IOException {
        /*
         * |<encoding>| <length-of-contents>|              <contents>                            |
         * | 4 bytes  |            4 bytes  | 2 bytes element| 4 bytes element | 8 bytes element |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        Set<byte[]> set = new ByteArraySet();
        int encoding = BaseRdbParser.LenHelper.encoding(stream);
        long lenOfContent = BaseRdbParser.LenHelper.lenOfContent(stream);
        for (long i = 0; i < lenOfContent; i++) {
            switch (encoding) {
                case 2:
                    String element = String.valueOf(stream.readInt(2));
                    set.add(element.getBytes());
                    break;
                case 4:
                    element = String.valueOf(stream.readInt(4));
                    set.add(element.getBytes());
                    break;
                case 8:
                    element = String.valueOf(stream.readLong(8));
                    set.add(element.getBytes());
                    break;
                default:
                    throw new AssertionError("expect encoding [2,4,8] but:" + encoding);
            }
        }
        return (T) set;
    }

    @Override
    public <T> T parseZSetZipList(RedisInputStream in, int version) throws IOException {
        /*
         * |<zlbytes>| <zltail>| <zllen>| <entry> ...<entry> | <zlend>|
         * | 4 bytes | 4 bytes | 2bytes | zipListEntry ...   | 1byte  |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        Set<ZSetEntry> zset = new LinkedHashSet<>();
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        while (zllen > 0) {
            byte[] element = BaseRdbParser.StringHelper.zipListEntry(stream);
            zllen--;
            double score = Double.valueOf(Strings.toString(BaseRdbParser.StringHelper.zipListEntry(stream)));
            zllen--;
            zset.add(new ZSetEntry(element, score));
        }
        int zlend = BaseRdbParser.LenHelper.zlend(stream);
        if (zlend != 255) {
            throw new AssertionError("zlend expect 255 but " + zlend);
        }
        return (T) zset;
    }

    @Override
    public <T> T parseHashZipList(RedisInputStream in, int version) throws IOException {
        /*
         * |<zlbytes>| <zltail>| <zllen>| <entry> ...<entry> | <zlend>|
         * | 4 bytes | 4 bytes | 2bytes | zipListEntry ...   | 1byte  |
         */
        BaseRdbParser parser = new BaseRdbParser(in);

        RedisInputStream stream = new RedisInputStream(parser.rdbLoadPlainStringObject());
        ByteArrayMap map = new ByteArrayMap();
        BaseRdbParser.LenHelper.zlbytes(stream); // zlbytes
        BaseRdbParser.LenHelper.zltail(stream); // zltail
        int zllen = BaseRdbParser.LenHelper.zllen(stream);
        while (zllen > 0) {
            byte[] field = BaseRdbParser.StringHelper.zipListEntry(stream);
            zllen--;
            byte[] value = BaseRdbParser.StringHelper.zipListEntry(stream);
            zllen--;
            map.put(field, value);
        }
        int zlend = BaseRdbParser.LenHelper.zlend(stream);
        if (zlend != 255) {
            throw new AssertionError("zlend expect 255 but " + zlend);
        }
        return (T) map;
    }

    @Override
    public <T> T parseListQuickList(RedisInputStream in, int version) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);

        long len = parser.rdbLoadLen().len;
        List<byte[]> list = new ByteArrayList();
        for (long i = 0; i < len; i++) {
            RedisInputStream stream = new RedisInputStream(parser.rdbGenericLoadStringObject(Constants.RDB_LOAD_NONE));
            // zlbytes
            BaseRdbParser.LenHelper.zlbytes(stream);
            // zltail
            BaseRdbParser.LenHelper.zltail(stream);
            int zllen = BaseRdbParser.LenHelper.zllen(stream);
            for (int j = 0; j < zllen; j++) {
                byte[] e = BaseRdbParser.StringHelper.zipListEntry(stream);
                list.add(e);
            }
            int zlend = BaseRdbParser.LenHelper.zlend(stream);
            if (zlend != 255) {
                throw new AssertionError("zlend expect 255 but " + zlend);
            }
        }
        return (T) list;
    }

    @Override
    public <T> T parseModule(RedisInputStream in, int version) throws IOException {
        //|6|6|6|6|6|6|6|6|6|10|
        BaseRdbParser parser = new BaseRdbParser(in);

        char[] c = new char[9];
        long moduleid = parser.rdbLoadLen().len;
        for (int i = 0; i < c.length; i++) {
            c[i] = Constants.MODULE_SET[(int) (moduleid >>> (10 + (c.length - 1 - i) * 6) & 63)];
        }
        String moduleName = new String(c);
        int moduleVersion = (int) (moduleid & 1023);
        ModuleParser<? extends Module> moduleParser = lookupModuleParser(moduleName, moduleVersion);
        if (moduleParser == null) {
            throw new NoSuchElementException("module parser[" + moduleName + ", " + moduleVersion + "] not register. rdb type: [RDB_TYPE_MODULE]");
        }
        Module module = moduleParser.parse(in, 1);
        return (T) module;
    }

    @Override
    public <T> T parseModule2(RedisInputStream in, int version) throws IOException {
        //|6|6|6|6|6|6|6|6|6|10|
        BaseRdbParser parser = new BaseRdbParser(in);

        char[] c = new char[9];
        long moduleid = parser.rdbLoadLen().len;
        for (int i = 0; i < c.length; i++) {
            c[i] = Constants.MODULE_SET[(int) (moduleid >>> (10 + (c.length - 1 - i) * 6) & 63)];
        }
        String moduleName = new String(c);
        int moduleVersion = (int) (moduleid & 1023);
        ModuleParser<? extends Module> moduleParser = lookupModuleParser(moduleName, moduleVersion);
        Module module = null;
        if (moduleParser == null) {
            log.warn("module parser[{}, {}] not register. rdb type: [RDB_TYPE_MODULE_2]. module parse skipped.", moduleName, moduleVersion);
            SkipRdbParser skipRdbParser = new SkipRdbParser(in);
            skipRdbParser.rdbLoadCheckModuleValue();
        } else {
            module = moduleParser.parse(in, 2);
            long eof = parser.rdbLoadLen().len;
            if (eof != Constants.RDB_MODULE_OPCODE_EOF) {
                throw new UnsupportedOperationException("The RDB file contains module data for the module '" + moduleName + "' that is not terminated by the proper module value EOF marker");
            }
        }
        return (T) module;
    }


    protected ModuleParser<? extends Module> lookupModuleParser(String moduleName, int moduleVersion) {
        return replication.getModuleParser(moduleName, moduleVersion);
    }
    @Override
    @SuppressWarnings("resource")
    public <T> T parseStreamListPacks(RedisInputStream in, int version) throws IOException {
        BaseRdbParser parser = new BaseRdbParser(in);

        Stream stream = new Stream();

        // Entries
        NavigableMap<StreamID, StreamEntry> entries = new TreeMap<>(StreamID.COMPARATOR);
        long listPacks = parser.rdbLoadLen().len;
        while (listPacks-- > 0) {
            RedisInputStream rawId = new RedisInputStream(parser.rdbLoadPlainStringObject());
            StreamID baseId = new StreamID(rawId.readLong(8, false), rawId.readLong(8, false));
            RedisInputStream listPack = new RedisInputStream(parser.rdbLoadPlainStringObject());
            listPack.skip(4); // total-bytes
            listPack.skip(2); // num-elements
            /*
             * Master entry
             * +-------+---------+------------+---------+--/--+---------+---------+-+
             * | count | deleted | num-fields | field_1 | field_2 | ... | field_N |0|
             * +-------+---------+------------+---------+--/--+---------+---------+-+
             */
            long count = Long.parseLong(Strings.toString(listPackEntry(listPack))); // count
            long deleted = Long.parseLong(Strings.toString(listPackEntry(listPack))); // deleted
            int numFields = Integer.parseInt(Strings.toString(listPackEntry(listPack))); // num-fields
            byte[][] tempFields = new byte[numFields][];
            for (int i = 0; i < numFields; i++) {
                tempFields[i] = listPackEntry(listPack);
            }
            listPackEntry(listPack); // 0

            long total = count + deleted;
            while (total-- > 0) {
                Map<byte[], byte[]> fields = new ByteArrayMap();
                /*
                 * FLAG
                 * +-----+--------+
                 * |flags|entry-id|
                 * +-----+--------+
                 */
                int flag = Integer.parseInt(Strings.toString(listPackEntry(listPack)));
                long ms = Long.parseLong(Strings.toString(listPackEntry(listPack)));
                long seq = Long.parseLong(Strings.toString(listPackEntry(listPack)));
                StreamID id = baseId.delta(ms, seq);
                boolean delete = (flag & Constants.STREAM_ITEM_FLAG_DELETED) != 0;
                if ((flag & Constants.STREAM_ITEM_FLAG_SAMEFIELDS) != 0) {
                    /*
                     * SAMEFIELD
                     * +-------+-/-+-------+--------+
                     * |value-1|...|value-N|lp-count|
                     * +-------+-/-+-------+--------+
                     */
                    for (int i = 0; i < numFields; i++) {
                        byte[] value = listPackEntry(listPack);
                        byte[] field = tempFields[i];
                        fields.put(field, value);
                    }
                    entries.put(id, new StreamEntry(id, delete, fields));
                } else {
                    /*
                     * NONEFIELD
                     * +----------+-------+-------+-/-+-------+-------+--------+
                     * |num-fields|field-1|value-1|...|field-N|value-N|lp-count|
                     * +----------+-------+-------+-/-+-------+-------+--------+
                     */
                    numFields = Integer.parseInt(Strings.toString(listPackEntry(listPack)));
                    for (int i = 0; i < numFields; i++) {
                        byte[] field = listPackEntry(listPack);
                        byte[] value = listPackEntry(listPack);
                        fields.put(field, value);
                    }
                    entries.put(id, new StreamEntry(id, delete, fields));
                }
                listPackEntry(listPack); // lp-count
            }
            int lpend = listPack.read(); // lp-end
            if (lpend != 255) {
                throw new AssertionError("listpack expect 255 but " + lpend);
            }
        }

        long length = parser.rdbLoadLen().len;
        StreamID lastId = new StreamID(parser.rdbLoadLen().len, parser.rdbLoadLen().len);

        // Group
        List<StreamGroup> groups = new ArrayList<>();
        long groupCount = parser.rdbLoadLen().len;
        while (groupCount-- > 0) {
            StreamGroup group = new StreamGroup();
            byte[] groupName = parser.rdbLoadPlainStringObject().first();
            StreamID groupLastId = new StreamID(parser.rdbLoadLen().len, parser.rdbLoadLen().len);

            // Group PEL
            NavigableMap<StreamID, StreamNack> groupPendingEntries = new TreeMap<>(StreamID.COMPARATOR);
            long globalPel = parser.rdbLoadLen().len;
            while (globalPel-- > 0) {
                StreamID rawId = new StreamID(in.readLong(8, false), in.readLong(8, false));
                long deliveryTime = parser.rdbLoadMillisecondTime();
                long deliveryCount = parser.rdbLoadLen().len;
                groupPendingEntries.put(rawId, new StreamNack(rawId, null, deliveryTime, deliveryCount));
            }

            // Consumer
            List<StreamConsumer> consumers = new ArrayList<>();
            long consumerCount = parser.rdbLoadLen().len;
            while (consumerCount-- > 0) {
                StreamConsumer consumer = new StreamConsumer();
                byte[] consumerName = parser.rdbLoadPlainStringObject().first();
                long seenTime = parser.rdbLoadMillisecondTime();

                // Consumer PEL
                NavigableMap<StreamID, StreamNack> consumerPendingEntries = new TreeMap<>(StreamID.COMPARATOR);
                long pel = parser.rdbLoadLen().len;
                while (pel-- > 0) {
                    StreamID rawId = new StreamID(in.readLong(8, false), in.readLong(8, false));
                    StreamNack nack = groupPendingEntries.get(rawId);
                    nack.setConsumer(consumer);
                    consumerPendingEntries.put(rawId, nack);
                }

                consumer.setName(consumerName);
                consumer.setSeenTime(seenTime);
                consumer.setPendingEntries(consumerPendingEntries);
                consumers.add(consumer);
            }

            group.setName(groupName);
            group.setLastId(groupLastId);
            group.setPendingEntries(groupPendingEntries);
            group.setConsumers(consumers);
            groups.add(group);
        }

        stream.setLastId(lastId);
        stream.setEntries(entries);
        stream.setLength(length);
        stream.setGroups(groups);

        return (T) stream;
    }
}
