package syncer.replica.rdb;

import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.*;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;
import syncer.replica.rdb.datatype.ContextKeyValuePair;
import syncer.replica.rdb.datatype.DB;
import syncer.replica.replication.AbstractReplication;
import syncer.replica.util.objectutil.Tuples;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static syncer.replica.constant.Constants.*;
import static syncer.replica.entity.Status.CONNECTED;

/**
 * @author zhanenqiang
 * @Description RDB文件协议解析器
 * @Date 2020/8/7
 */

@Slf4j
public class RedisRdbParser {

    protected final RedisInputStream in;
    protected final RedisRdbVisitor rdbVisitor;
    protected final AbstractReplication replicator;

    public RedisRdbParser(RedisInputStream in, AbstractReplication replicator) {
        this.in = in;
        this.replicator = replicator;
        this.rdbVisitor = this.replicator.getRdbVisitor();
    }

    /**
     * The RDB E-BNF
     * <p>
     * RDB        =    'REDIS', $version, [AUX], [MODULE_AUX], {SELECTDB, [RESIZEDB], {RECORD}}, '0xFF', [$checksum];
     * <p>
     * RECORD     =    [EXPIRED], [IDLE | FREQ], KEY, VALUE;
     * <p>
     * SELECTDB   =    '0xFE', $length;
     * <p>
     * AUX        =    {'0xFA', $string, $string};            (*Introduced in rdb version 7*)
     * <p>
     * MODULE_AUX =    {'0xF7', $length};                     (*Introduced in rdb version 9*)
     * <p>
     * RESIZEDB   =    '0xFB', $length, $length;              (*Introduced in rdb version 7*)
     * <p>
     * EXPIRED    =    ('0xFD', $second) | ('0xFC', $millisecond);
     * <p>
     * IDLE       =    {'0xF8', $value-type};                 (*Introduced in rdb version 9*)
     * <p>
     * FREQ       =    {'0xF9', $length};                     (*Introduced in rdb version 9*)
     * <p>
     * KEY        =    $string;
     * <p>
     * VALUE      =    $value-type, ( $string
     * <p>
     * | $list
     * <p>
     * | $set
     * <p>
     * | $zset
     * <p>
     * | $hash
     * <p>
     * | $zset2                  (*Introduced in rdb version 8*)
     * <p>
     * | $module                 (*Introduced in rdb version 8*)
     * <p>
     * | $module2                (*Introduced in rdb version 8*)
     * <p>
     * | $hashzipmap
     * <p>
     * | $listziplist
     * <p>
     * | $setintset
     * <p>
     * | $zsetziplist
     * <p>
     * | $hashziplist
     * <p>
     * | $listquicklist          (*Introduced in rdb version 7*)
     * <p>
     * | $streamlistpacks);      (*Introduced in rdb version 9*)
     * <p>
     *
     * @return read bytes
     * @throws IOException when read timeout
     */
    public long parse() throws IOException, IncrementException {
        /*
         * ----------------------------
         * 52 45 44 49 53              # Magic String "REDIS"
         * 30 30 30 33                 # RDB Version Number in big endian. In this case, version = 0003 = 3
         * ----------------------------
         */
        long offset = 0L;
        this.replicator.submitEvent(new PreRdbSyncEvent(), Tuples.of(0L, 0L));
        this.replicator.submitSyncerTaskEvent(SyncerTaskEvent
                .builder()
                .taskStatusType(TaskStatusType.RDBRUNING)
                .offset(replicator.getConfiguration().getReplOffset())
                .replid(replicator.getConfiguration().getReplId())
                .event(SyncerEvent.builder().taskId(replicator.getConfiguration().getTaskId()).build())
                .msg("RDBRUNING")
                .build());

        in.mark();
        rdbVisitor.applyMagic(in);
        int version = rdbVisitor.applyVersion(in);
        offset += in.unmark();
        DB db = null;
        /*
         * rdb
         */
        loop:
        while (this.replicator.getStatus() == CONNECTED) {
            Event event = null;
            in.mark();
            int type = rdbVisitor.applyType(in);
            ContextKeyValuePair kv = new ContextKeyValuePair();
            kv.setDb(db);
            switch (type) {
                case RDB_OPCODE_EXPIRETIME:
                    event = rdbVisitor.applyExpireTime(in, version, kv);
                    break;
                case RDB_OPCODE_EXPIRETIME_MS:
                    event = rdbVisitor.applyExpireTimeMs(in, version, kv);
                    break;
                case RDB_OPCODE_FREQ:
                    event = rdbVisitor.applyFreq(in, version, kv);
                    break;
                case RDB_OPCODE_IDLE:
                    event = rdbVisitor.applyIdle(in, version, kv);
                    break;
                case RDB_OPCODE_AUX:
                    event = rdbVisitor.applyAux(in, version);
                    break;
                case RDB_OPCODE_MODULE_AUX:
                    event = rdbVisitor.applyModuleAux(in, version);
                    break;
                case RDB_OPCODE_RESIZEDB:
                    rdbVisitor.applyResizeDB(in, version, kv);
                    break;
                case RDB_OPCODE_SELECTDB:
                    db = rdbVisitor.applySelectDB(in, version);
                    break;
                case RDB_OPCODE_EOF:
                    long checksum = rdbVisitor.applyEof(in, version);
                    long start = offset;
                    offset += in.unmark();
                    this.replicator.submitEvent(new PostRdbSyncEvent(checksum), Tuples.of(start, offset));
                    break loop;
                case RDB_TYPE_STRING:
                    event = rdbVisitor.applyString(in, version, kv);
                    break;
                case RDB_TYPE_LIST:
                    event = rdbVisitor.applyList(in, version, kv);
                    break;
                case RDB_TYPE_SET:
                    event = rdbVisitor.applySet(in, version, kv);
                    break;
                case RDB_TYPE_ZSET:
                    event = rdbVisitor.applyZSet(in, version, kv);
                    break;
                case RDB_TYPE_ZSET_2:
                    event = rdbVisitor.applyZSet2(in, version, kv);
                    break;
                case RDB_TYPE_HASH:
                    event = rdbVisitor.applyHash(in, version, kv);
                    break;
                case RDB_TYPE_HASH_ZIPMAP:
                    event = rdbVisitor.applyHashZipMap(in, version, kv);
                    break;
                case RDB_TYPE_LIST_ZIPLIST:
                    event = rdbVisitor.applyListZipList(in, version, kv);
                    break;
                case RDB_TYPE_SET_INTSET:
                    event = rdbVisitor.applySetIntSet(in, version, kv);
                    break;
                case RDB_TYPE_ZSET_ZIPLIST:
                    event = rdbVisitor.applyZSetZipList(in, version, kv);
                    break;
                case RDB_TYPE_HASH_ZIPLIST:
                    event = rdbVisitor.applyHashZipList(in, version, kv);
                    break;
                case RDB_TYPE_LIST_QUICKLIST:
                    event = rdbVisitor.applyListQuickList(in, version, kv);
                    break;
                case RDB_TYPE_MODULE:
                    event = rdbVisitor.applyModule(in, version, kv);
                    break;
                case RDB_TYPE_MODULE_2:
                    event = rdbVisitor.applyModule2(in, version, kv);
                    break;
                case RDB_TYPE_STREAM_LISTPACKS:
                    event = rdbVisitor.applyStreamListPacks(in, version, kv);
                    break;
                default:
                    throw  new IncrementException("unexpected value type:" + type + ", check your ModuleParser or ValueIterableRdbVisitor.");
//                    throw new AssertionError("unexpected value type:" + type + ", check your ModuleParser or ValueIterableRdbVisitor.");
            }
            long start = offset;
            offset += in.unmark();
            if (event == null){
                continue;
            }
            if (replicator.verbose() && log.isDebugEnabled()) {
                log.debug("{}", event);
            }
            this.replicator.submitEvent(event, Tuples.of(start, offset));
        }
        return offset;
    }
}
