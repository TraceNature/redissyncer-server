package syncer.transmission.tikv.processor;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.tikv.common.TiConfiguration;
import org.tikv.common.TiSession;
import org.tikv.raw.RawKVClient;
import org.tikv.shade.com.google.protobuf.ByteString;
import org.tikv.txn.TxnKVClient;
import syncer.replica.datatype.command.ExistType;
import syncer.replica.datatype.command.common.PingCommand;
import syncer.replica.datatype.command.common.SelectCommand;
import syncer.replica.datatype.command.set.SetCommand;
import syncer.replica.datatype.command.set.XATType;
import syncer.replica.event.KeyStringValueStringEvent;
import syncer.replica.event.end.PostCommandSyncEvent;
import syncer.replica.event.end.PostRdbSyncEvent;
import syncer.replica.event.start.PreCommandSyncEvent;
import syncer.replica.event.start.PreRdbSyncEvent;
import syncer.replica.exception.TikvKeyErrorException;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.ExpiredType;
import syncer.transmission.tikv.TikvKeyType;
import syncer.transmission.tikv.parser.TikvKeyNameParser;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 默认处理器
 */
@Slf4j
public class DefaultRedis2TikvProcessor implements IRedis2TikvProcessor{
    private String taskId;
    private String instId;
    private String tikvUri;
    private RawKVClient kvClient;
    @Builder.Default
    private AtomicLong dbNum=new AtomicLong(0);
    private TikvKeyNameParser tikvKeyNameParser;

    @Override
    public void load(String taskId,String instId, String tikvUri) {
        this.taskId=taskId;
        this.instId=instId;
        this.tikvUri=tikvUri;
        if(Objects.isNull(this.kvClient)){
            this.kvClient=tikvRawClient(tikvUri);
        }
        this.tikvKeyNameParser=new TikvKeyNameParser();
    }

    @Override
    public void close() {
        if(Objects.nonNull(kvClient)){
            kvClient.close();
        }
    }

    @Override
    public void preRdbSyncEventHandler(PreRdbSyncEvent event) {
        log.info("[{}][TASKID {}]增量同步开始..",instId,taskId);
    }

    @Override
    public void postRdbSyncEventHandler(PostRdbSyncEvent event) {
        log.info("[{}][TASKID {}]全量同步结束..",instId,taskId);
    }

    /**
     * rdb String 处理器
     * @param event
     */
    @Override
    public void rdbStringHandler(KeyStringValueStringEvent event) {
        changeDb(event.getDb().getCurrentDbNumber());
        String key=tikvKeyNameParser.getStringKey(instId,dbNum.get(), TikvKeyType.STRING,event.getKey());
        if(event.getExpiredType().equals(ExpiredType.NONE)){
            kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()));
        }else {
            kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()),event.getExpiredSeconds());
        }
        log.info("[{}][TASKID {}]写入tikv key [{}] value [{}]",instId,taskId,key, Strings.byteToString(event.getValue()));
        String value=kvClient.get(ByteString.copyFromUtf8(key)).toStringUtf8();
        try {
            log.info("[{}][TASKID {}]读出tikv key [{}] value [{}]",instId,taskId,tikvKeyNameParser.parser(key.getBytes(),TikvKeyType.STRING),value);
        } catch (TikvKeyErrorException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void preCommandSyncEventHandler(PreCommandSyncEvent event) {
        log.info("[{}][TASKID {}]增量同步开始..",instId,taskId);
    }

    @Override
    public void postCommandSyncEventHandler(PostCommandSyncEvent event) {
        log.info("[{}][TASKID {}]增量同步结束..",instId,taskId);
    }

    /**
     * set 命令
     *
     * https://redis.io/commands/set
     *
     * SET key value [EX seconds|PX milliseconds|EXAT timestamp|PXAT milliseconds-timestamp|KEEPTTL] [NX|XX] [GET]
     *
     *   nx XX | none
     *     nx Don't update already existing elements
     *     xx Only update elements that already exist
     *   EXAT  | PXAT | none
     *
     *
     * @param event
     */
    @Override
    public void setCommandHandler(SetCommand event) {
        String key=tikvKeyNameParser.getStringKey(instId,dbNum.get(),TikvKeyType.STRING,event.getKey());
        try {
            if(ExistType.NONE.equals(event.getExistType())){
                if(ExpiredType.NONE.equals(event.getExpiredType())&&XATType.NONE.equals(event.getXatType())){
                    kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()));
                }else {
                    Long ttl=setCommandTtl(event);
                    kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()),ttl);
                }
            }else if(ExistType.NX.equals(event.getExistType())){
                ByteString value=kvClient.get(ByteString.copyFromUtf8(key));
                if(value.isEmpty()){
                    if(ExpiredType.NONE.equals(event.getExpiredType())&&XATType.NONE.equals(event.getXatType())){
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()));
                    }else {
                        Long ttl=setCommandTtl(event);
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()),ttl);
                    }
                }
            }else if(ExistType.XX.equals(event.getExistType())){
                ByteString value=kvClient.get(ByteString.copyFromUtf8(key));
                if(!value.isEmpty()){
                    if(ExpiredType.NONE.equals(event.getExpiredType())&&XATType.NONE.equals(event.getXatType())){
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()));
                    }else {
                        Long ttl=setCommandTtl(event);
                        kvClient.put(ByteString.copyFromUtf8(key),ByteString.copyFrom(event.getValue()),ttl);
                    }
                }
            }

            log.info("[{}][TASKID {}]写入tikv key[{}] value[{}] ttl [{}]",instId,taskId,key,event.getValue(),setCommandTtl(event));
        }catch (Exception e){
            log.error("[{}][TASKID {}]写入tikv key[{}]失败",instId,taskId,key);
        }

//        String value=kvClient.get(ByteString.copyFromUtf8(key)).toStringUtf8();
//        System.out.println("读出tikv key ["+key+"] value ["+value+"]");

    }


    /**
     * set命令计算ttl
     * @param event
     * @return
     */
    long setCommandTtl(SetCommand event){
        Long ttl=-1L;
        if(ExpiredType.MS.equals(event.getExpiredType())){
            ttl=event.getExpiredValue()/1000;
        }else if(ExpiredType.SECOND.equals(event.getExpiredType())){
            ttl=event.getExpiredValue();
        }else if(ExpiredType.NONE.equals(event.getExpiredType())){
            // EXAT  | PXAT | none
            //PXAT timestamp-milliseconds
            if(XATType.PXAT.equals(event.getXatType())){
                ttl=(event.getXatValue()-System.currentTimeMillis())/1000;
            }else if(XATType.EXAT.equals(event.getXatType())){
                //EXAT timestamp-seconds
                ttl=(event.getXatValue()-System.currentTimeMillis()/1000);
            }
        }
        return ttl;
    }


    /**
     * select命令 ---> 更新当前 db号
     * @param event
     */
    @Override
    public void selectCommandHandler(SelectCommand event) {
        changeDb(event.getCurrentNumber());
    }

    /**
     * Ping
     * @param event
     */
    @Override
    public void pingCommandHandler(PingCommand event) {

    }


    void changeDb(long currentDbNumber){
        if(currentDbNumber!=dbNum.get()){
            dbNum.set(currentDbNumber);
        }
    }


    RawKVClient tikvRawClient(String address){
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



}
