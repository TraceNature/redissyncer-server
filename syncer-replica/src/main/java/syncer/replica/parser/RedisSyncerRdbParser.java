package syncer.replica.parser;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.constant.Constants;
import syncer.replica.context.ContextKeyValue;
import syncer.replica.entity.RedisDB;
import syncer.replica.event.Event;
import syncer.replica.event.SyncerTaskEvent;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;
import syncer.replica.replication.AbstractReplication;
import syncer.replica.status.TaskStatus;
import syncer.replica.util.tuple.Tuples;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class RedisSyncerRdbParser {
    protected final RedisInputStream in;
    protected final IRdbParser rdbParser;
    protected final AbstractReplication replication;

    public RedisSyncerRdbParser(RedisInputStream in, AbstractReplication replication) {
        this.in = in;
        this.replication = replication;
        this.rdbParser = this.replication.getRdbVisitor();
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

        long offset = 0L;
        this.replication.submitEvent(new PreRdbSyncEvent(), Tuples.of(0L, 0L));
        this.replication.setStatus(TaskStatus.RDBRUNNING);
        this.replication.submitSyncerTaskEvent(SyncerTaskEvent
                .builder()
                .taskId(replication.getConfig().getTaskId())
                .offset(replication.getConfig().getReplOffset())
                .replid(replication.getConfig().getReplId())
                .event(TaskStatus.RDBRUNNING)
                .msg("RDBRUNING")
                .build());
        in.mark();
        rdbParser.parseMagic(in);
        int version = rdbParser.parseRdbVersion(in);
        //rdb verion mapping

        log.info("[TASKID {}] source redis node rdb version: {}",replication.getConfig().getTaskId(),version);
        offset += in.unmark();
        RedisDB db = null;
        loop:
        while (isRunning()) {
            Event event = null;
            in.mark();
            int type = rdbParser.parseValueType(in);
            ContextKeyValue kv = new ContextKeyValue();
            kv.setDb(db);
            switch (type) {
                //FD $unsigned int
                case Constants.RDB_OPCODE_EXPIRETIME:
                    event = rdbParser.parseExpireTime(in, version, kv);
                    break;
                //FC $unsigned long
                case Constants.RDB_OPCODE_EXPIRETIME_MS:
                    event = rdbParser.parseExpireTimeMs(in, version, kv);
                    break;
                case Constants.RDB_OPCODE_FREQ:
                    event = rdbParser.parseFreq(in, version, kv);
                    break;
                case Constants.RDB_OPCODE_IDLE:
                    event = rdbParser.parseIdle(in, version, kv);
                    break;
                case Constants.RDB_OPCODE_AUX:
                    event = rdbParser.parseAux(in, version);
                    break;
                case Constants.RDB_OPCODE_MODULE_AUX:
                    event = rdbParser.parseModuleAux(in, version);
                    break;
                case Constants.RDB_OPCODE_RESIZEDB:
                    rdbParser.parseResizeDB(in, version, kv);

                    break;
                case Constants.RDB_OPCODE_SELECTDB:
                    db = rdbParser.parseCurrentDb(in, version);
                    break;
                //RDB结束
                case Constants.RDB_OPCODE_EOF:
                    log.info("rdb parser end");
                    long checksum = rdbParser.parseEof(in, version);
                    long start = offset;
                    offset += in.unmark();
                    this.replication.submitEvent(new PostRdbSyncEvent(checksum), Tuples.of(start, offset));
                    break loop;
                case Constants.RDB_TYPE_STRING:
                    event = rdbParser.parseString(in, version, kv);
                    break;
                case Constants.RDB_TYPE_LIST:
                    event = rdbParser.parseList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_SET:
                    event = rdbParser.parseSet(in, version, kv);
                    break;
                case Constants.RDB_TYPE_ZSET:
                    event = rdbParser.parseZSet(in, version, kv);
                    break;
                case Constants.RDB_TYPE_ZSET_2:
                    event = rdbParser.parseZSet2(in, version, kv);
                    break;
                case Constants.RDB_TYPE_HASH:
                    event = rdbParser.parseHash(in, version, kv);
                    break;
                case Constants.RDB_TYPE_HASH_ZIPMAP:
                    event = rdbParser.parseHashZipMap(in, version, kv);
                    break;
                case Constants.RDB_TYPE_LIST_ZIPLIST:
                    event = rdbParser.parseListZipList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_SET_INTSET:
                    event = rdbParser.parseSetIntSet(in, version, kv);
                    break;
                case Constants.RDB_TYPE_ZSET_ZIPLIST:
                    event = rdbParser.parseZSetZipList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_HASH_ZIPLIST:
                    event = rdbParser.parseHashZipList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_LIST_QUICKLIST:
                    event = rdbParser.parseListQuickList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_MODULE:
                    event = rdbParser.parseModule(in, version, kv);
                    break;
                case Constants.RDB_TYPE_MODULE_2:
                    event = rdbParser.parseModule2(in, version, kv);
                    break;
                case Constants.RDB_TYPE_STREAM_LISTPACKS:
                    event = rdbParser.parseStreamListPacks(in, version, kv);
                    break;
                default:

                    throw new IncrementException("unexpected value type:" + type + ", check your ModuleParser or ValueIterableRdbVisitor.");
            }
            long start = offset;
            offset += in.unmark();
            if (Objects.isNull(event)) {
                continue;
            }
            this.replication.submitEvent(event, Tuples.of(start, offset),replication.getConfig().getReplId(),offset);
        }
        return offset;
    }
/**

    public long parse() throws IOException, IncrementException {
        long offset = 0L;
        this.replication.submitEvent(new PreRdbSyncEvent(), Tuples.of(0L, 0L));
        this.replication.setStatus(TaskStatus.RDBRUNNING);
        this.replication.submitSyncerTaskEvent(SyncerTaskEvent
                .builder()
                .taskId(replication.getConfig().getTaskId())
                .offset(replication.getConfig().getReplOffset())
                .replid(replication.getConfig().getReplId())
                .event(TaskStatus.RDBRUNNING)
                .msg("RDBRUNING")
                .build());
        in.mark();
        rdbParser.parseMagic(in);
        int version = rdbParser.parseRdbVersion(in);
        //rdb verion mapping

        log.info("[TASKID {}] source redis node rdb version: {}",replication.getConfig().getTaskId(),version);
        offset += in.unmark();
        RedisDB db = null;
        loop:
        while (isRunning()) {
            Event event = null;
            in.mark();
            int type = rdbParser.parseValueType(in);
            ContextKeyValue kv = new ContextKeyValue();
            kv.setDb(db);
            switch (type) {
                //FD $unsigned int
                case Constants.RDB_OPCODE_EXPIRETIME:
                    event = rdbParser.parseExpireTime(in, version, kv);
                    break;
                //FC $unsigned long
                case Constants.RDB_OPCODE_EXPIRETIME_MS:
                    event = rdbParser.parseExpireTimeMs(in, version, kv);
                    break;
                case Constants.RDB_OPCODE_FREQ:
                    event = rdbParser.parseFreq(in, version, kv);
                    break;
                case Constants.RDB_OPCODE_IDLE:
                    event = rdbParser.parseIdle(in, version, kv);
                    break;
                case Constants.RDB_OPCODE_AUX:
                    event = rdbParser.parseAux(in, version);
                    break;
                case Constants.RDB_OPCODE_MODULE_AUX:
                    event = rdbParser.parseModuleAux(in, version);
                    break;
                case Constants.RDB_OPCODE_RESIZEDB:
                    rdbParser.parseResizeDB(in, version, kv);

                    break;
                case Constants.RDB_OPCODE_SELECTDB:
                    db = rdbParser.parseCurrentDb(in, version);
                    break;
                //RDB结束
                case Constants.RDB_OPCODE_EOF:
                    log.info("rdb parser end");
                    long checksum = rdbParser.parseEof(in, version);
                    long start = offset;
                    offset += in.unmark();
                    this.replication.submitEvent(new PostRdbSyncEvent(checksum), Tuples.of(start, offset));
                    break loop;
                case Constants.RDB_TYPE_STRING:
                    event = rdbParser.parseString(in, version, kv);
                    break;
                case Constants.RDB_TYPE_LIST:
                    event = rdbParser.parseList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_SET:
                    event = rdbParser.parseSet(in, version, kv);
                    break;
                case Constants.RDB_TYPE_ZSET:
                    event = rdbParser.parseZSet(in, version, kv);
                    break;
                case Constants.RDB_TYPE_ZSET_2:
                    event = rdbParser.parseZSet2(in, version, kv);
                    break;
                case Constants.RDB_TYPE_HASH:
                    event = rdbParser.parseHash(in, version, kv);
                    break;
                case Constants.RDB_TYPE_HASH_ZIPMAP:
                    event = rdbParser.parseHashZipMap(in, version, kv);
                    break;
                case Constants.RDB_TYPE_LIST_ZIPLIST:
                    event = rdbParser.parseListZipList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_SET_INTSET:
                    event = rdbParser.parseSetIntSet(in, version, kv);
                    break;
                case Constants.RDB_TYPE_ZSET_ZIPLIST:
                    event = rdbParser.parseZSetZipList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_HASH_ZIPLIST:
                    event = rdbParser.parseHashZipList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_LIST_QUICKLIST:
                    event = rdbParser.parseListQuickList(in, version, kv);
                    break;
                case Constants.RDB_TYPE_MODULE:
                    event = rdbParser.parseModule(in, version, kv);
                    break;
                case Constants.RDB_TYPE_MODULE_2:
                    event = rdbParser.parseModule2(in, version, kv);
                    break;
                case Constants.RDB_TYPE_STREAM_LISTPACKS:
                    event = rdbParser.parseStreamListPacks(in, version, kv);
                    break;
                default:

                    throw new IncrementException("unexpected value type:" + type + ", check your ModuleParser or ValueIterableRdbVisitor.");
            }
            long start = offset;
            offset += in.unmark();
            if (Objects.isNull(event)) {
                continue;
            }
            this.replication.submitEvent(event, Tuples.of(start, offset),replication.getConfig().getReplId(),offset);
        }
        return offset;
    }

    **/

    private boolean isRunning() {
        return replication.getStatus().equals(TaskStatus.RDBRUNNING)
                || replication.getStatus().equals(TaskStatus.COMMANDRUNNING);
    }

}
