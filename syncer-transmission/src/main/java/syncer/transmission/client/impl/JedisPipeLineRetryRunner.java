package syncer.transmission.client.impl;

import syncer.jedis.exceptions.JedisConnectionException;
import syncer.transmission.compensator.PipeLineCompensatorEnum;
import syncer.transmission.entity.EventEntity;

import java.util.ArrayList;
import java.util.List;

public class JedisPipeLineRetryRunner implements JedisRetryRunner{
    private JedisPipeLineClient client;

    public JedisPipeLineRetryRunner(JedisPipeLineClient client) {
        this.client = client;
    }

    @Override
    public void run() throws JedisConnectionException {
        //runner
        client.targetClient=client.createJedis(client.host,client.port,client.password);
        client.pipelined = client.targetClient.pipelined();

        List<EventEntity> dataList=new ArrayList<>();
        dataList.addAll(client.kvPersistence.getKeys());
        client.kvPersistence.clear();
        client.commandNums.set(0);
        for (int i=0;i<dataList.size();i++){
            EventEntity eventEntity= dataList.get(i);
            if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET)){
                client.set(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET_WITH_TIME)){
                client.set(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValue(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.APPEND)){
                client.append(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH)){
                client.lpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_WITH_TIME)){
                client.lpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_LIST)){
                client.lpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getLpush_value());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.LPUSH_WITH_TIME_LIST)){
                client.lpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getLpush_value());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH)){
                client.rpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH_WITH_TIME)){
                client.rpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH_LIST)){
                client.rpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getLpush_value());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RPUSH_WITH_TIME_LIST)){
                client.rpush(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD)){
                client.sadd(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_WITH_TIME)){
                client.sadd(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_SET)){
                client.sadd(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMembers());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SADD_WITH_TIME_SET)){
                client.sadd(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getMembers());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.ZADD)){
                client.zadd(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getZaddValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.ZADD_WITH_TIME)){
                client.zadd(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getZaddValue(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.HMSET)){
                client.hmset(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getHash_value());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.HMSET_WITH_TIME)){
                client.hmset(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getHash_value(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RESTORE)){
                client.restore(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValue());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.RESTORREPLCE)){
                client.restoreReplace(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs(),eventEntity.getValue(),eventEntity.isHighVersion());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.COMMAND)){
                client.send(eventEntity.getCmd(),eventEntity.getValueList());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SELECT)){
                client.select(Math.toIntExact(eventEntity.getDbNum()));
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.PEXPIRE)){
                client.pexpire(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getMs());
            }else if(eventEntity.getPipeLineCompensatorEnum().equals(PipeLineCompensatorEnum.SET_WITH_TIME)){
                client.set(eventEntity.getDbNum(),eventEntity.getKey(),eventEntity.getValue(),eventEntity.getMs());
            }

            int num = client.commandNums.incrementAndGet();
            if (num >= client.count) {
                List<Object> resultList = client.pipelined.syncAndReturnAll();

                //补偿入口
                client.commitCompensator(resultList);
            }
        }

    }
}
