/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package syncer.syncerplusredis.replicator;

import syncer.syncerplusredis.cmd.Command;
import syncer.syncerplusredis.cmd.CommandName;
import syncer.syncerplusredis.cmd.CommandParser;


import syncer.syncerplusredis.cmd.parser.AppendParser;

import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.io.RedisInputStream;


import syncer.syncerplusredis.event.AbstractEvent;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.rdb.DefaultRdbVisitor;
import syncer.syncerplusredis.rdb.RdbVisitor;
import syncer.syncerplusredis.rdb.datatype.Module;
import syncer.syncerplusredis.rdb.module.ModuleKey;
import syncer.syncerplusredis.rdb.module.ModuleParser;
import syncer.syncerplusredis.util.type.Tuple2;
import syncer.syncerplusredis.util.type.Tuples;
import syncer.syncerplusredis.cmd.parser.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static syncer.syncerplusredis.util.type.Tuples.of;

/**
 * @author Leon Chen
 * @version 2.1.1
 * @since 2.1.0
 */


public abstract class AbstractReplicator extends AbstractReplicatorListener implements Replicator {
    protected Configuration configuration;
    protected RedisInputStream inputStream;
    protected RdbVisitor rdbVisitor = new DefaultRdbVisitor(this);
    protected final AtomicBoolean manual = new AtomicBoolean(false);
    protected final AtomicReference<Status> connected = new AtomicReference<>(Status.DISCONNECTED);
    protected final Map<ModuleKey, ModuleParser<? extends Module>> modules = new ConcurrentHashMap<>();
    protected final Map<CommandName, CommandParser<? extends Command>> commands = new ConcurrentHashMap<>();
    
    @Override
    public CommandParser<? extends Command> getCommandParser(CommandName command) {
        return commands.get(command);
    }
    
    @Override
    public <T extends Command> void addCommandParser(CommandName command, CommandParser<T> parser) {
        commands.put(command, parser);
    }
    
    @Override
    public CommandParser<? extends Command> removeCommandParser(CommandName command) {
        return commands.remove(command);
    }
    
    @Override
    public ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion) {
        return modules.get(ModuleKey.key(moduleName, moduleVersion));
    }
    
    @Override
    public <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser) {
        modules.put(ModuleKey.key(moduleName, moduleVersion), parser);
    }
    
    @Override
    public ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion) {
        return modules.remove(ModuleKey.key(moduleName, moduleVersion));
    }

    public void submitEvent(Event event) {
        long offset = configuration.getReplOffset();
        submitEvent(event, Tuples.of(offset, offset));
    }

    public void submitEvent(Event event, Tuple2<Long, Long> offsets) {
        try {
            dress(event, offsets);
            doEventListener(this, event);
        } catch (UncheckedIOException e) {
            throw e;
            //ignore UncheckedIOException so that to propagate to caller.
        } catch (Throwable e) {
            doExceptionListener(this, e, event);
        }
    }

    protected void dress(Event event, Tuple2<Long, Long> offsets) {
        if (event instanceof AbstractEvent) {
            ((AbstractEvent) event).getContext().setOffsets(offsets);
        }
    }

    protected boolean compareAndSet(Status prev, Status next) {
        boolean result = connected.compareAndSet(prev, next);
        if (result) {
            doStatusListener(this, next);
        }
        return result;
    }
    
    protected void setStatus(Status next) {
        connected.set(next);
        doStatusListener(this, next);
    }
    
    @Override
    public boolean verbose() {
        return configuration != null && configuration.isVerbose();
    }
    
    @Override
    public Status getStatus() {
        return connected.get();
    }
    
    @Override
    public Configuration getConfiguration() {
        return configuration;
    }
    
    @Override
    public void setRdbVisitor(RdbVisitor rdbVisitor) {
        this.rdbVisitor = rdbVisitor;
    }
    
    @Override
    public RdbVisitor getRdbVisitor() {
        return this.rdbVisitor;
    }
    
    @Override
    public void builtInCommandParserRegister() {
        addCommandParser(CommandName.name("PING"), new PingParser());
        addCommandParser(CommandName.name("APPEND"), new AppendParser());
        addCommandParser(CommandName.name("SET"), new SetParser());
        addCommandParser(CommandName.name("SETEX"), new SetExParser());
        addCommandParser(CommandName.name("MSET"), new MSetParser());
        addCommandParser(CommandName.name("DEL"), new DelParser());
        addCommandParser(CommandName.name("SADD"), new SAddParser());
        addCommandParser(CommandName.name("HMSET"), new HMSetParser());
        addCommandParser(CommandName.name("HSET"), new HSetParser());
        addCommandParser(CommandName.name("LSET"), new LSetParser());
        addCommandParser(CommandName.name("EXPIRE"), new ExpireParser());
        addCommandParser(CommandName.name("EXPIREAT"), new ExpireAtParser());
        addCommandParser(CommandName.name("GETSET"), new GetSetParser());
        addCommandParser(CommandName.name("HSETNX"), new HSetNxParser());
        addCommandParser(CommandName.name("MSETNX"), new MSetNxParser());
        addCommandParser(CommandName.name("PSETEX"), new PSetExParser());
        addCommandParser(CommandName.name("SETNX"), new SetNxParser());
        addCommandParser(CommandName.name("SETRANGE"), new SetRangeParser());
        addCommandParser(CommandName.name("HDEL"), new HDelParser());
        addCommandParser(CommandName.name("LPOP"), new LPopParser());
        addCommandParser(CommandName.name("LPUSH"), new LPushParser());
        addCommandParser(CommandName.name("LPUSHX"), new LPushXParser());
        addCommandParser(CommandName.name("LRem"), new LRemParser());
        addCommandParser(CommandName.name("RPOP"), new RPopParser());
        addCommandParser(CommandName.name("RPUSH"), new RPushParser());
        addCommandParser(CommandName.name("RPUSHX"), new RPushXParser());
        addCommandParser(CommandName.name("ZREM"), new ZRemParser());
        addCommandParser(CommandName.name("RENAME"), new RenameParser());
        addCommandParser(CommandName.name("INCR"), new IncrParser());
        addCommandParser(CommandName.name("DECR"), new DecrParser());
        addCommandParser(CommandName.name("INCRBY"), new IncrByParser());
        addCommandParser(CommandName.name("DECRBY"), new DecrByParser());
        addCommandParser(CommandName.name("PERSIST"), new PersistParser());
        addCommandParser(CommandName.name("SELECT"), new SelectParser());
        addCommandParser(CommandName.name("FLUSHALL"), new FlushAllParser());
        addCommandParser(CommandName.name("FLUSHDB"), new FlushDBParser());
        addCommandParser(CommandName.name("HINCRBY"), new HIncrByParser());
        addCommandParser(CommandName.name("ZINCRBY"), new ZIncrByParser());
        addCommandParser(CommandName.name("MOVE"), new MoveParser());
        addCommandParser(CommandName.name("SMOVE"), new SMoveParser());
        addCommandParser(CommandName.name("PFADD"), new PFAddParser());
        addCommandParser(CommandName.name("PFCOUNT"), new PFCountParser());
        addCommandParser(CommandName.name("PFMERGE"), new PFMergeParser());
        addCommandParser(CommandName.name("SDIFFSTORE"), new SDiffStoreParser());
        addCommandParser(CommandName.name("SINTERSTORE"), new SInterStoreParser());
        addCommandParser(CommandName.name("SUNIONSTORE"), new SUnionStoreParser());
        addCommandParser(CommandName.name("ZADD"), new ZAddParser());
        addCommandParser(CommandName.name("ZINTERSTORE"), new ZInterStoreParser());
        addCommandParser(CommandName.name("ZUNIONSTORE"), new ZUnionStoreParser());
        addCommandParser(CommandName.name("BRPOPLPUSH"), new BRPopLPushParser());
        addCommandParser(CommandName.name("LINSERT"), new LInsertParser());
        addCommandParser(CommandName.name("RENAMENX"), new RenameNxParser());
        addCommandParser(CommandName.name("RESTORE"), new RestoreParser());
        addCommandParser(CommandName.name("PEXPIRE"), new PExpireParser());
        addCommandParser(CommandName.name("PEXPIREAT"), new PExpireAtParser());
        addCommandParser(CommandName.name("GEOADD"), new GeoAddParser());
        addCommandParser(CommandName.name("EVAL"), new EvalParser());
        addCommandParser(CommandName.name("EVALSHA"), new EvalShaParser());
        addCommandParser(CommandName.name("SCRIPT"), new ScriptParser());
        addCommandParser(CommandName.name("PUBLISH"), new PublishParser());
        addCommandParser(CommandName.name("BITOP"), new BitOpParser());
        addCommandParser(CommandName.name("BITFIELD"), new BitFieldParser());
        addCommandParser(CommandName.name("SETBIT"), new SetBitParser());
        addCommandParser(CommandName.name("SREM"), new SRemParser());
        addCommandParser(CommandName.name("UNLINK"), new UnLinkParser());
        addCommandParser(CommandName.name("SWAPDB"), new SwapDBParser());
        addCommandParser(CommandName.name("MULTI"), new MultiParser());
        addCommandParser(CommandName.name("EXEC"), new ExecParser());
        addCommandParser(CommandName.name("ZREMRANGEBYSCORE"), new ZRemRangeByScoreParser());
        addCommandParser(CommandName.name("ZREMRANGEBYRANK"), new ZRemRangeByRankParser());
        addCommandParser(CommandName.name("ZREMRANGEBYLEX"), new ZRemRangeByLexParser());
        addCommandParser(CommandName.name("LTRIM"), new LTrimParser());
        addCommandParser(CommandName.name("SORT"), new SortParser());
        addCommandParser(CommandName.name("RPOPLPUSH"), new RPopLPushParser());
        addCommandParser(CommandName.name("ZPOPMIN"), new ZPopMinParser());
        addCommandParser(CommandName.name("ZPOPMAX"), new ZPopMaxParser());
        addCommandParser(CommandName.name("REPLCONF"), new ReplConfParser());
        addCommandParser(CommandName.name("XACK"), new XAckParser());
        addCommandParser(CommandName.name("XADD"), new XAddParser());
        addCommandParser(CommandName.name("XCLAIM"), new XClaimParser());
        addCommandParser(CommandName.name("XDEL"), new XDelParser());
        addCommandParser(CommandName.name("XGROUP"), new XGroupParser());
        addCommandParser(CommandName.name("XTRIM"), new XTrimParser());
        addCommandParser(CommandName.name("XSETID"), new XSetIdParser());
    }

    @Override
    public void open() throws IOException, IncrementException {
        manual.compareAndSet(true, false);
    }
    
    @Override
    public void close() throws IOException {
        compareAndSet(Status.CONNECTED, Status.DISCONNECTING);
        manual.compareAndSet(false, true);
    }
    
    protected boolean isClosed() {
        return manual.get();
    }
    
    protected void doClose() throws IOException {
        compareAndSet(Status.CONNECTED, Status.DISCONNECTING);
        try {
            if (inputStream != null) {
                this.inputStream.setRawByteListeners(null);
                inputStream.close();
            }
        } catch (IOException ignore) {
            /*NOP*/
        } finally {
            setStatus(Status.DISCONNECTED);
        }
    }
}
