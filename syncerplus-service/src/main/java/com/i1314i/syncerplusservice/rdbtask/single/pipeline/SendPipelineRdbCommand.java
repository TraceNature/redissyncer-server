package com.i1314i.syncerplusservice.rdbtask.single.pipeline;

import com.i1314i.syncerplusredis.event.Event;
import com.i1314i.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import com.i1314i.syncerplusredis.rdb.iterable.datatype.*;
import com.i1314i.syncerplusservice.constant.RedisCommandTypeEnum;
import com.i1314i.syncerplusservice.entity.EventEntity;
import com.i1314i.syncerplusservice.entity.SyncTaskEntity;
import com.i1314i.syncerplusservice.entity.thread.EventTypeEntity;
import com.i1314i.syncerplusservice.util.Jedis.JDJedis;
import com.i1314i.syncerplusservice.util.common.RedisCommon;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.SetParams;

import java.util.Map;

public class SendPipelineRdbCommand {
    public synchronized void  sendSingleCommand(Long ms, RedisCommandTypeEnum typeEnum, Event event, Pipeline pipeline, String key, double redisVersion, SyncTaskEntity taskEntity) {
        if (ms == null || ms == 0L) {
            if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                if (valueString.getBatch() == 0) {
                    pipeline.set(valueString.getKey(), valueString.getValue());
                } else {
                    pipeline.append(valueString.getKey(), valueString.getValue());
                }
                taskEntity.add();
            } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                byte[][] array = RedisCommon.listBytes(valueList.getValue());
                pipeline.lpush(valueList.getKey(), array);
                taskEntity.add(valueList.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {

                BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                byte[][] array = RedisCommon.setBytes(valueSet.getValue());
                pipeline.sadd(valueSet.getKey(), array);
                taskEntity.add(valueSet.getValue().size());

            } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                Map<byte[], Double> map = RedisCommon.zsetBytes(valueZSet.getValue());

                pipeline.zadd(valueZSet.getKey(), map);
                taskEntity.add(valueZSet.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {

                BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;

                pipeline.hmset(valueHash.getKey(), valueHash.getValue());

                taskEntity.add(valueHash.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
                DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
                if (redisVersion < 3.0) {

                    pipeline.del(valueDump.getKey());
                    pipeline.restore(valueDump.getKey(), 0, valueDump.getValue());
                } else {
                    pipeline.restoreReplace(valueDump.getKey(), 0, valueDump.getValue());
                }

                taskEntity.add();
            }

        } else {
            if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                if (valueString.getBatch() == 0) {
                    pipeline.set(valueString.getKey(), valueString.getValue(), new SetParams().px(ms));
                } else {
                    pipeline.append(valueString.getKey(), valueString.getValue());
                }
                taskEntity.add();
            } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                byte[][] array = RedisCommon.listBytes(valueList.getValue());
                pipeline.lpush(valueList.getKey(), array);
                pipeline.pexpire(valueList.getKey(), ms);
                taskEntity.add(valueList.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {
                BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                byte[][] array = RedisCommon.setBytes(valueSet.getValue());
                pipeline.sadd(valueSet.getKey(), array);
                pipeline.pexpire(valueSet.getKey(), ms);
                taskEntity.add(valueSet.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                Map<byte[], Double> map = RedisCommon.zsetBytes(valueZSet.getValue());
                pipeline.zadd(valueZSet.getKey(), map);
                pipeline.pexpire(valueZSet.getKey(), ms);
                taskEntity.add(valueZSet.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {

                BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                pipeline.hmset(valueHash.getKey(), valueHash.getValue());
                pipeline.pexpire(valueHash.getKey(), ms);
                taskEntity.add(valueHash.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
                DumpKeyValuePair valueDump = (DumpKeyValuePair) event;
                if (redisVersion< 3.0) {

                    pipeline.del(valueDump.getKey());
                    pipeline.restore(valueDump.getKey(), 0, valueDump.getValue());
                } else {
                    pipeline.restoreReplace(valueDump.getKey(), 0, valueDump.getValue());
                }
                taskEntity.add();
            }
        }
    }
    public synchronized void  sendSingleCommand(Long ms, RedisCommandTypeEnum typeEnum, Event event, PipelineLock pipeline, String key, double redisVersion, SyncTaskEntity taskEntity) {

        if (ms == null || ms == 0L) {
            if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                if (valueString.getBatch() == 0) {
                    pipeline.set(valueString.getKey(), valueString.getValue());
                } else {
                    pipeline.append(valueString.getKey(), valueString.getValue());
                }

                EventEntity eventEntity=new EventEntity(valueString.getKey(),ms,valueString.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                taskEntity.addKey(eventEntity);

                taskEntity.add();
            } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                byte[][] array = RedisCommon.listBytes(valueList.getValue());
                pipeline.lpush(valueList.getKey(), array);

                if(valueList.isLast()&&valueList.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueList.getKey(),ms,valueList.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.LIST);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueList.getKey(),ms,valueList.getDb(), EventTypeEntity.ABANDON,RedisCommandTypeEnum.LIST);
                    taskEntity.addKey(eventEntity);
                }

                taskEntity.add(valueList.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {

                BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                byte[][] array = RedisCommon.setBytes(valueSet.getValue());
                pipeline.sadd(valueSet.getKey(), array);
                taskEntity.add(valueSet.getValue().size());


                if(valueSet.isLast()&&valueSet.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueSet.getKey(),ms,valueSet.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueSet.getKey(),ms, valueSet.getDb(),EventTypeEntity.ABANDON,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }

            } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                Map<byte[], Double> map = RedisCommon.zsetBytes(valueZSet.getValue());

                pipeline.zadd(valueZSet.getKey(), map);
                taskEntity.add(valueZSet.getValue().size());

                if(valueZSet.isLast()&&valueZSet.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueZSet.getKey(),ms,valueZSet.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueZSet.getKey(),ms, valueZSet.getDb(),EventTypeEntity.ABANDON,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }

            } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {

                BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;

                pipeline.hmset(valueHash.getKey(), valueHash.getValue());

                taskEntity.add(valueHash.getValue().size());

                if(valueHash.isLast()&&valueHash.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueHash.getKey(),ms,valueHash.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.HASH);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueHash.getKey(),ms, valueHash.getDb(),EventTypeEntity.ABANDON,RedisCommandTypeEnum.HASH);
                    taskEntity.addKey(eventEntity);
                }

            } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {



                DumpKeyValuePair valueDump = (DumpKeyValuePair) event;

                if (redisVersion < 3.0) {

                    pipeline.del(valueDump.getKey());
                    EventEntity eventEntity=new EventEntity("DEL".getBytes(),ms,valueDump.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                    taskEntity.addKey(eventEntity);
                    pipeline.restore(valueDump.getKey(), 0, valueDump.getValue());
                } else {
                    pipeline.restoreReplace(valueDump.getKey(), 0, valueDump.getValue());
                }

                taskEntity.add();

                EventEntity eventEntity=new EventEntity(valueDump.getKey(),ms,valueDump.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                taskEntity.addKey(eventEntity);
            }

        } else {

            if (typeEnum.equals(RedisCommandTypeEnum.STRING)) {
                BatchedKeyStringValueString valueString = (BatchedKeyStringValueString) event;
                if (valueString.getBatch() <=2) {
                    pipeline.set(valueString.getKey(), valueString.getValue(), new SetParams().px(ms));
                } else {
                    pipeline.append(valueString.getKey(), valueString.getValue());


                }

                EventEntity eventEntity=new EventEntity(valueString.getKey(),ms,valueString.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                taskEntity.addKey(eventEntity);

                taskEntity.add();
            } else if (typeEnum.equals(RedisCommandTypeEnum.LIST)) {
                BatchedKeyStringValueList valueList = (BatchedKeyStringValueList) event;
                byte[][] array = RedisCommon.listBytes(valueList.getValue());
                pipeline.lpush(valueList.getKey(), array);
                pipeline.pexpire(valueList.getKey(), ms);
                if(valueList.isLast()&&valueList.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueList.getKey(),ms,valueList.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.LIST);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueList.getKey(),ms,valueList.getDb(), EventTypeEntity.ABANDON,RedisCommandTypeEnum.LIST);
                    taskEntity.addKey(eventEntity);
                }

                taskEntity.add(valueList.getValue().size());
            } else if (typeEnum.equals(RedisCommandTypeEnum.SET)) {
                BatchedKeyStringValueSet valueSet = (BatchedKeyStringValueSet) event;
                byte[][] array = RedisCommon.setBytes(valueSet.getValue());
                pipeline.sadd(valueSet.getKey(), array);
                pipeline.pexpire(valueSet.getKey(), ms);
                taskEntity.add(valueSet.getValue().size());


                if(valueSet.isLast()&&valueSet.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueSet.getKey(),ms,valueSet.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueSet.getKey(),ms, valueSet.getDb(),EventTypeEntity.ABANDON,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.ZSET)) {
                BatchedKeyStringValueZSet valueZSet = (BatchedKeyStringValueZSet) event;
                Map<byte[], Double> map = RedisCommon.zsetBytes(valueZSet.getValue());
                pipeline.zadd(valueZSet.getKey(), map);
                pipeline.pexpire(valueZSet.getKey(), ms);

                taskEntity.add(valueZSet.getValue().size());

                if(valueZSet.isLast()&&valueZSet.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueZSet.getKey(),ms,valueZSet.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueZSet.getKey(),ms, valueZSet.getDb(),EventTypeEntity.ABANDON,RedisCommandTypeEnum.SET);
                    taskEntity.addKey(eventEntity);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.HASH)) {

                BatchedKeyStringValueHash valueHash = (BatchedKeyStringValueHash) event;
                pipeline.hmset(valueHash.getKey(), valueHash.getValue());
                pipeline.pexpire(valueHash.getKey(), ms);
                taskEntity.add(valueHash.getValue().size());

                taskEntity.add(valueHash.getValue().size());

                if(valueHash.isLast()&&valueHash.getBatch()<=2){
                    EventEntity eventEntity=new EventEntity(valueHash.getKey(),ms,valueHash.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.HASH);
                    taskEntity.addKey(eventEntity);
                }else {
                    EventEntity eventEntity=new EventEntity(valueHash.getKey(),ms, valueHash.getDb(),EventTypeEntity.ABANDON,RedisCommandTypeEnum.HASH);
                    taskEntity.addKey(eventEntity);
                }
            } else if (typeEnum.equals(RedisCommandTypeEnum.DUMP)) {
                DumpKeyValuePair valueDump = (DumpKeyValuePair) event;

                if (redisVersion< 3.0) {

                    pipeline.del(valueDump.getKey());
                    pipeline.restore(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                } else {
                    pipeline.restoreReplace(valueDump.getKey(), Math.toIntExact(ms), valueDump.getValue());
                }
                taskEntity.add();
                EventEntity eventEntity=new EventEntity(valueDump.getKey(),ms,valueDump.getDb(), EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                taskEntity.addKey(eventEntity);

            }
        }
    }
}
