package syncer.transmission.tikv;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.tikv.common.TiConfiguration;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;
import org.tikv.txn.TxnKVClient;
import syncer.common.util.TemplateUtils;
import syncer.jedis.Jedis;
import syncer.replica.config.RedisURI;
import syncer.replica.datatype.command.set.SetCommand;
import syncer.replica.event.Event;
import syncer.replica.event.KeyStringValueListEvent;
import syncer.replica.event.KeyStringValueStringEvent;
import syncer.replica.exception.TikvKeyErrorException;
import syncer.replica.listener.EventListener;
import syncer.replica.register.DefaultCommandRegister;
import syncer.replica.register.TikvCommandRegister;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.ExpiredType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

@Slf4j
public class RedisToTikvMain {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Replication replication = null;
        String sourceRedisUri=redisUri("127.0.0.1:6379","");
        RedisURI suri = new RedisURI(sourceRedisUri);
        replication = new RedisReplication(suri, true);
        replication.getConfig().setTaskId(TemplateUtils.uuid());
        String instId="testARedis";
        String spliceN="[|-$-|]";
        RawKVClient kvClient=tikvRawClient("114.67.120.120:2379");

        Jedis cle=new Jedis("127.0.0.1",6379);
        cle.set("hh hh","jjj");
        cle.close();
        /**
         * 注册解析器
         */
        TikvCommandRegister.addCommandParser(replication);
        TikvKeyNameParser tikvKeyNameParser=new TikvKeyNameParser();
        int dbNum=0;
        try {


        replication.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replication replication, Event event) {

                System.out.println(event.getClass());
                //String rdb
                if(event instanceof KeyStringValueStringEvent){
                    KeyStringValueStringEvent stringValueStringEvent= (KeyStringValueStringEvent) event;
//                    System.out.println("key: "+ Strings.byteToString(stringValueStringEvent.getKey()));
//                    System.out.println("value: "+Strings.byteToString(stringValueStringEvent.getValue()));
                    String key=tikvKeyNameParser.getStringKey(instId,dbNum,TikvKeyType.STRING,stringValueStringEvent.getKey());

                    if(stringValueStringEvent.getExpiredType().equals(ExpiredType.NONE)){
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(stringValueStringEvent.getValue()));
                    }else {
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(stringValueStringEvent.getValue()),stringValueStringEvent.getExpiredMs());
                    }
                    System.out.println("写入tikv key ["+key+"] value ["+Strings.byteToString(stringValueStringEvent.getValue())+"]");
                    log.info("写入tikv key [{}] value [{}]",key,Strings.byteToString(stringValueStringEvent.getValue()));
                    String value=kvClient.get(ByteString.copyFromUtf8(key)).toStringUtf8();

                    try {
                        System.out.println("读出tikv  key ["+tikvKeyNameParser.parser(key.getBytes(),TikvKeyType.STRING)+"] value ["+value+"]");
                        log.info("读出tikv key [{}] value [{}]",tikvKeyNameParser.parser(key.getBytes(),TikvKeyType.STRING),value);
                    } catch (TikvKeyErrorException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(event.getClass());

                //List
                if(event instanceof KeyStringValueListEvent){
                    KeyStringValueListEvent stringValueListEvent= (KeyStringValueListEvent) event;
//                    System.out.println(stringValueListEvent.);
                }


                if(event instanceof SetCommand){
                    SetCommand setCommand= (SetCommand) event;
                    String key=tikvKeyNameParser.getStringKey(instId,dbNum,TikvKeyType.STRING,setCommand.getKey());
                    if(setCommand.getExpiredType().equals(ExpiredType.NONE)){
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(setCommand.getValue()));
                    }else {
                        Long ttl=setCommand.getExpiredValue();
                        if(ExpiredType.SECOND.equals(setCommand.getExpiredType())){
                            ttl=setCommand.getExpiredValue()*1000;
                        }
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(setCommand.getValue()),ttl);
                    }
                    System.out.println("写入tikv key ["+key+"] value ["+Strings.byteToString(setCommand.getValue())+"]");
                    String value=kvClient.get(ByteString.copyFromUtf8(key)).toStringUtf8();
                    System.out.println("读出tikv key ["+key+"] value ["+value+"]");
                }





            }

            @Override
            public String eventListenerName() {
                return null;
            }
        });

        }finally {
            if(Objects.nonNull(kvClient)){
                kvClient.close();
            }
        }
        replication.open();

    }


    static RawKVClient tikvRawClient(String address){
        TiConfiguration conf = TiConfiguration.createRawDefault(address);
        TiSession session = TiSession.create(conf);
        RawKVClient client = session.createRawClient();
        return client;
    }

    static TxnKVClient tikvTxnClient(String address){
        TiConfiguration conf = TiConfiguration.createRawDefault(address);
        TiSession session = TiSession.create(conf);
        TxnKVClient client = session.createTxnClient();
        return client;
    }

    public static String redisUri(String address, String password) {
        StringBuilder uri = new StringBuilder("redis://");
        int index = 0;
        if (address != null && address.length() > 0) {
            uri.append(address);
            if (password != null && password.length() > 0) {
                index++;
                uri.append("?authPassword=");
                uri.append(password);
            }
        }
        return uri.toString();
    }
}
