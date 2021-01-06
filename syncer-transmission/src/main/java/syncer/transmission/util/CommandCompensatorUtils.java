package syncer.transmission.util;

import lombok.extern.slf4j.Slf4j;
import syncer.jedis.exceptions.JedisDataException;
import syncer.replica.util.objectutil.Strings;
import syncer.transmission.constants.ComanndResponseType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 补偿机制相关工具 涉及到具体的命令
 * @Date 2020/7/9
 */
@Slf4j
public class CommandCompensatorUtils {
    Map<String,ComanndResponseType> comanndResponseTypeMap=new ConcurrentHashMap<>();
    Map<String,ComanndResponseType>alwaysTrueComanndMap=new ConcurrentHashMap<>();


    public CommandCompensatorUtils() {
        initStringCommand();
        initListCommand();
        initHashCommand();
        initSetCommand();
        initSysCommand();
        initTransactionCommand();
        initZSetCommand();
        initPubSubCommand();
        initAlwaysTrueComannds();
        initHyperLogLogCommands();
    }


    /**
     * 初始化String 类型命令
     */
    void initStringCommand(){
        /**
         * String 类型命令
         */
        comanndResponseTypeMap.put("SET", ComanndResponseType.builder().type(1).command("SET").commandResponse("OK").build());
        comanndResponseTypeMap.put("SETBIT",ComanndResponseType.builder().type(2).command("SETBIT").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SETEX",ComanndResponseType.builder().type(1).command("SETEX").commandResponse("OK").build());
        comanndResponseTypeMap.put("SETNX",ComanndResponseType.builder().type(2).command("SETNX").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SETRANGE",ComanndResponseType.builder().type(2).command("SETRANGE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("STRLEN",ComanndResponseType.builder().type(2).command("STRLEN").commandResponse("LONG").build());
        comanndResponseTypeMap.put("PSETEX",ComanndResponseType.builder().type(1).command("PSETEX").commandResponse("OK").build());
        comanndResponseTypeMap.put("MSETNX",ComanndResponseType.builder().type(2).command("MSETNX").commandResponse("LONG").build());
        comanndResponseTypeMap.put("MSET",ComanndResponseType.builder().type(1).command("MSET").commandResponse("OK").build());
        comanndResponseTypeMap.put("APPEND",ComanndResponseType.builder().type(2).command("APPEND").commandResponse("LONG").build());
        comanndResponseTypeMap.put("BITCOUNT",ComanndResponseType.builder().type(2).command("BITCOUNT").commandResponse("LONG").build());
        comanndResponseTypeMap.put("BITOP",ComanndResponseType.builder().type(2).command("BITOP").commandResponse("LONG").build());
        comanndResponseTypeMap.put("DECR",ComanndResponseType.builder().type(3).command("DECR").commandResponse("LONG").build());
        comanndResponseTypeMap.put("INCR",ComanndResponseType.builder().type(3).command("INCR").commandResponse("LONG").build());
        comanndResponseTypeMap.put("INCRBY",ComanndResponseType.builder().type(3).command("INCRBY").commandResponse("LONG").build());
        comanndResponseTypeMap.put("INCRBYFLOAT",ComanndResponseType.builder().type(5).command("INCRBYFLOAT").commandResponse("DOUBLE").build());
        comanndResponseTypeMap.put("DECRBY",ComanndResponseType.builder().type(3).command("DECRBY").commandResponse("LONG").build());
        comanndResponseTypeMap.put("GETSET",ComanndResponseType.builder().type(4).command("GETSET").commandResponse("STRING").build());


    }

    /**
     * 初始化Hash类型命令
     */
    void initHashCommand(){
        comanndResponseTypeMap.put("HDEL",ComanndResponseType.builder().type(2).command("HDEL").commandResponse("LONG").build());
        comanndResponseTypeMap.put("HINCRBYFLOAT",ComanndResponseType.builder().type(5).command("HINCRBYFLOAT").commandResponse("DOUBLE").build());
        comanndResponseTypeMap.put("HINCRBY",ComanndResponseType.builder().type(2).command("HINCRBY").commandResponse("LONG").build());
        comanndResponseTypeMap.put("HMSET",ComanndResponseType.builder().type(1).command("HMSET").commandResponse("OK").build());
        comanndResponseTypeMap.put("HSET",ComanndResponseType.builder().type(2).command("HSET").commandResponse("LONG").build());
        comanndResponseTypeMap.put("HSETNX",ComanndResponseType.builder().type(2).command("HSETNX").commandResponse("LONG").build());
    }

    /**
     * 初始化List类型命令
     */
    void initListCommand(){
        comanndResponseTypeMap.put("BLPOP",ComanndResponseType.builder().type(6).command("BLPOP").commandResponse("ARRAYLIST").build());
        comanndResponseTypeMap.put("BRPOP",ComanndResponseType.builder().type(6).command("BRPOP").commandResponse("ARRAYLIST").build());
        comanndResponseTypeMap.put("BRPOPLPUSH",ComanndResponseType.builder().type(4).command("BRPOPLPUSH").commandResponse("STRING").build());
        comanndResponseTypeMap.put("LINSERT",ComanndResponseType.builder().type(3).command("LINSERT").commandResponse("LONG").build());
        comanndResponseTypeMap.put("LPOP",ComanndResponseType.builder().type(4).command("LPOP").commandResponse("STRING").build());
        comanndResponseTypeMap.put("LPUSH",ComanndResponseType.builder().type(2).command("LPUSH").commandResponse("LONG").build());
        comanndResponseTypeMap.put("LPUSHX",ComanndResponseType.builder().type(2).command("LPUSHX").commandResponse("LONG").build());
        comanndResponseTypeMap.put("LREM",ComanndResponseType.builder().type(2).command("LREM").commandResponse("LONG").build());
        comanndResponseTypeMap.put("LSET", ComanndResponseType.builder().type(1).command("LSET").commandResponse("OK").build());
        comanndResponseTypeMap.put("LTRIM", ComanndResponseType.builder().type(1).command("LTRIM").commandResponse("OK").build());
        comanndResponseTypeMap.put("RPOP",ComanndResponseType.builder().type(4).command("RPOP").commandResponse("STRING").build());
        comanndResponseTypeMap.put("RPOPLPUSH",ComanndResponseType.builder().type(4).command("RPOPLPUSH").commandResponse("STRING").build());
        comanndResponseTypeMap.put("RPUSH",ComanndResponseType.builder().type(2).command("RPUSH").commandResponse("LONG").build());
        comanndResponseTypeMap.put("RPUSHX",ComanndResponseType.builder().type(2).command("RPUSHX").commandResponse("LONG").build());
    }

    /**
     * 初始化Set类型命令
     */
    void initSetCommand(){
        comanndResponseTypeMap.put("SADD",ComanndResponseType.builder().type(2).command("SADD").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SCARD",ComanndResponseType.builder().type(2).command("SCARD").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SDIFF",ComanndResponseType.builder().type(7).command("SDIFF").commandResponse("HashSet").build());
        comanndResponseTypeMap.put("SDIFFSTORE",ComanndResponseType.builder().type(2).command("SDIFFSTORE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SINTER",ComanndResponseType.builder().type(7).command("SINTER").commandResponse("HashSet").build());
        comanndResponseTypeMap.put("SINTERSTORE",ComanndResponseType.builder().type(2).command("SINTERSTORE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SISMEMBER",ComanndResponseType.builder().type(2).command("SISMEMBER").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SMOVE",ComanndResponseType.builder().type(2).command("SMOVE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SPOP",ComanndResponseType.builder().type(4).command("SPOP").commandResponse("STRING").build());
        comanndResponseTypeMap.put("SREM",ComanndResponseType.builder().type(2).command("SREM").commandResponse("LONG").build());
        comanndResponseTypeMap.put("SUNIONSTORE",ComanndResponseType.builder().type(2).command("SUNIONSTORE").commandResponse("LONG").build());
    }


    /**
     * 初始化有序Set命令集合
     */
    void initZSetCommand(){
        comanndResponseTypeMap.put("ZADD",ComanndResponseType.builder().type(2).command("ZADD").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZCARD",ComanndResponseType.builder().type(2).command("ZCARD").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZCOUNT",ComanndResponseType.builder().type(2).command("ZCOUNT").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZINCRBY",ComanndResponseType.builder().type(9).command("ZINCRBY").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZRANGEBYSCORE",ComanndResponseType.builder().type(7).command("ZRANGEBYSCORE").commandResponse("HashSet").build());
        comanndResponseTypeMap.put("ZRANK",ComanndResponseType.builder().type(2).command("ZRANK").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZREM",ComanndResponseType.builder().type(2).command("ZREM").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZREMRANGEBYRANK",ComanndResponseType.builder().type(2).command("ZREMRANGEBYRANK").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZREMRANGEBYSCORE",ComanndResponseType.builder().type(2).command("ZREMRANGEBYSCORE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZREVRANK",ComanndResponseType.builder().type(2).command("ZREVRANK").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZUNIONSTORE",ComanndResponseType.builder().type(2).command("ZUNIONSTORE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("ZINTERSTORE",ComanndResponseType.builder().type(2).command("ZINTERSTORE").commandResponse("LONG").build());

        comanndResponseTypeMap.put("ZPOPMAX",ComanndResponseType.builder().type(6).command("ZPOPMAX").commandResponse("LIST").build());
        comanndResponseTypeMap.put("ZPOPMIN",ComanndResponseType.builder().type(6).command("ZPOPMIN").commandResponse("LIST").build());

    }




    /**
     * 初始化订阅相关命令集合
     */
    void initPubSubCommand(){
        comanndResponseTypeMap.put("PUBLISH",ComanndResponseType.builder().type(2).command("PUBLISH").commandResponse("LONG").build());
    }

    /**
     * 事务相关
     */
    void initTransactionCommand(){
        comanndResponseTypeMap.put("DISCARD",ComanndResponseType.builder().type(1).command("DISCARD").commandResponse("OK").build());
        comanndResponseTypeMap.put("MULTI",ComanndResponseType.builder().type(1).command("MULTI").commandResponse("OK").build());
        comanndResponseTypeMap.put("UNWATCH",ComanndResponseType.builder().type(1).command("UNWATCH").commandResponse("OK").build());
        comanndResponseTypeMap.put("WATCH",ComanndResponseType.builder().type(1).command("WATCH").commandResponse("OK").build());

        //EXEC

    }

    void initSysCommand(){
        comanndResponseTypeMap.put("SELECT",ComanndResponseType.builder().type(1).command("SELECT").commandResponse("OK").build());
        comanndResponseTypeMap.put("PING",ComanndResponseType.builder().type(8).command("PING").commandResponse("PONG").build());
        comanndResponseTypeMap.put("AUTH",ComanndResponseType.builder().type(1).command("AUTH").commandResponse("OK").build());
        comanndResponseTypeMap.put("QUIT",ComanndResponseType.builder().type(1).command("QUIT").commandResponse("OK").build());
        comanndResponseTypeMap.put("FLUSHALL",ComanndResponseType.builder().type(1).command("FLUSHALL").commandResponse("OK").build());
        comanndResponseTypeMap.put("FLUSHDB",ComanndResponseType.builder().type(1).command("FLUSHDB").commandResponse("OK").build());
        comanndResponseTypeMap.put("CLIENT KILL",ComanndResponseType.builder().type(1).command("CLIENT KILL").commandResponse("OK").build());
        comanndResponseTypeMap.put("CONFIG RESETSTAT",ComanndResponseType.builder().type(1).command("CONFIG RESETSTAT").commandResponse("OK").build());
        comanndResponseTypeMap.put("SLAVEOF",ComanndResponseType.builder().type(1).command("SLAVEOF").commandResponse("OK").build());



        comanndResponseTypeMap.put("DEL",ComanndResponseType.builder().type(2).command("DEL").commandResponse("LONG").build());
        comanndResponseTypeMap.put("EXPIRE",ComanndResponseType.builder().type(2).command("EXPIRE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("EXPIREAT",ComanndResponseType.builder().type(2).command("EXPIREAT").commandResponse("LONG").build());
        comanndResponseTypeMap.put("MIGRATE",ComanndResponseType.builder().type(1).command("MIGRATE").commandResponse("OK").build());
        comanndResponseTypeMap.put("MOVE",ComanndResponseType.builder().type(2).command("MOVE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("PERSIST",ComanndResponseType.builder().type(2).command("PERSIST").commandResponse("LONG").build());
        comanndResponseTypeMap.put("PEXPIRE",ComanndResponseType.builder().type(2).command("PEXPIRE").commandResponse("LONG").build());
        comanndResponseTypeMap.put("PEXPIREAT",ComanndResponseType.builder().type(2).command("PEXPIREAT").commandResponse("LONG").build());
        comanndResponseTypeMap.put("RENAME",ComanndResponseType.builder().type(1).command("RENAME").commandResponse("OK").build());
        comanndResponseTypeMap.put("RENAMENX",ComanndResponseType.builder().type(1).command("RENAMENX").commandResponse("OK").build());
        comanndResponseTypeMap.put("RESTORE",ComanndResponseType.builder().type(1).command("RESTORE").commandResponse("OK").build());
        comanndResponseTypeMap.put("RESTOREREPLACE",ComanndResponseType.builder().type(1).command("RESTOREREPLACE").commandResponse("OK").build());
    }


    /**
     * 初始化HyperLogLog命令
     */
    void  initHyperLogLogCommands(){
        comanndResponseTypeMap.put("PFMERGE", ComanndResponseType.builder().type(1).command("PFMERGE").commandResponse("OK").build());
        comanndResponseTypeMap.put("PFADD",ComanndResponseType.builder().type(2).command("PFADD").commandResponse("LONG").build());
        comanndResponseTypeMap.put("PFCOUNT",ComanndResponseType.builder().type(2).command("PFCOUNT").commandResponse("LONG").build());

    }


    /**
     * 不进行补偿的命令
     */
    void initAlwaysTrueComannds(){
        alwaysTrueComanndMap.put("BLPOP",ComanndResponseType.builder().type(6).command("BLPOP").commandResponse("ARRAYLIST").build());
        alwaysTrueComanndMap.put("BRPOP",ComanndResponseType.builder().type(6).command("BRPOP").commandResponse("ARRAYLIST").build());
        alwaysTrueComanndMap.put("BRPOPLPUSH",ComanndResponseType.builder().type(4).command("BRPOPLPUSH").commandResponse("STRING").build());
        alwaysTrueComanndMap.put("LINSERT",ComanndResponseType.builder().type(3).command("LINSERT").commandResponse("LONG").build());
        alwaysTrueComanndMap.put("LPOP",ComanndResponseType.builder().type(4).command("LPOP").commandResponse("STRING").build());
        alwaysTrueComanndMap.put("LPUSH",ComanndResponseType.builder().type(2).command("LPUSH").commandResponse("LONG").build());
        alwaysTrueComanndMap.put("LPUSHX",ComanndResponseType.builder().type(2).command("LPUSHX").commandResponse("LONG").build());
        alwaysTrueComanndMap.put("LREM",ComanndResponseType.builder().type(2).command("LREM").commandResponse("LONG").build());
        alwaysTrueComanndMap.put("LSET", ComanndResponseType.builder().type(1).command("LSET").commandResponse("OK").build());
        alwaysTrueComanndMap.put("LTRIM", ComanndResponseType.builder().type(1).command("LTRIM").commandResponse("OK").build());
        alwaysTrueComanndMap.put("RPOP",ComanndResponseType.builder().type(4).command("RPOP").commandResponse("STRING").build());
        alwaysTrueComanndMap.put("RPOPLPUSH",ComanndResponseType.builder().type(4).command("RPOPLPUSH").commandResponse("STRING").build());
        alwaysTrueComanndMap.put("RPUSH",ComanndResponseType.builder().type(2).command("RPUSH").commandResponse("LONG").build());
        alwaysTrueComanndMap.put("RPUSHX",ComanndResponseType.builder().type(2).command("RPUSHX").commandResponse("LONG").build());

    }

    public  boolean isObjectSuccess(Object res, String cmd){
        if(cmd.indexOf("[sendCommand]")>=0){
            cmd=cmd.split("[sendCommand]")[0];
        }
        String stringCmd=cmd.trim().toUpperCase();
        if(comanndResponseTypeMap.containsKey(stringCmd)){
            ComanndResponseType responseType=comanndResponseTypeMap.get(stringCmd);
            if(responseType.getType()==1){
                return isOK(res,stringCmd);
            }else if(responseType.getType()==2){
                return isLongOK(res);
            }else if(responseType.getType()==3){
                return isBigLongOK(res);
            }else if(responseType.getType()==4){
                return isString(res);
            }else if(responseType.getType()==5){
                return isDouble(res);
            }else if(responseType.getType()==6){
                return  isArrayList(res);
            }else if(responseType.getType()==7){
                return isHashSet(res);
            }else if(responseType.getType()==8){
                return isPong(res);
            }else if(responseType.getType()==9){
                return true;
            }


        }else{
            log.warn("[{}]command->type[{}]未被定义",stringCmd,cmd);
            return true;
        }

        return true;
    }


    public  boolean isCommandSuccess(Object res, byte[]cmd,String taskId,String key){

        String stringCmd= Strings.byteToString(cmd).toUpperCase();
        if(res==null){
            if("SET".equalsIgnoreCase(stringCmd)){
                return true;
            }
        }
        if(res instanceof byte[]){
            try {
                res=toObject((byte[]) res);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        boolean  result=isObjectSuccess(res,stringCmd);
        if(alwaysTrueComanndMap.containsKey(stringCmd)){
            if(!result){
                log.error("[{}]:  command:[{}],key: [{}],response[{}]未在补偿规则内，首次写入失败后不进行数据补偿",taskId,stringCmd,key,res);
            }
            return true;
        }
        return result;
    }



    /**
     * 数组转对象
     * @param bytes
     * @return
     */
    Object toObject (byte[] bytes) {
        String data= Strings.byteToString(bytes);
        try{
            Long longData= Long.valueOf(data);
            return longData;
        }catch (Exception e){

        }

        try{
            Integer longData= Integer.valueOf(data);
            return Long.valueOf(longData);
        }catch (Exception e){

        }

        try{
            Double longData= Double.valueOf(data);
            return longData;
        }catch (Exception e){

        }

        return data;
    }


    boolean isOK(Object res,String cmd){
        if(res instanceof JedisDataException){
            JedisDataException data= (JedisDataException) res;
            if(data.getMessage().equalsIgnoreCase("ERR no such key")){
                return true;
            }else if(data.getMessage().equalsIgnoreCase("ERR index out of range")){
                return true;
            }
        }


        if("OK".equalsIgnoreCase(String.valueOf(res))){
            return  true;
        }else{
            log.warn("String[{}],command[{}]",res,cmd);
            return false;
        }
    }

    boolean isLongOK(Object res){
        long data=Long.valueOf(String.valueOf(res)).longValue();
        if(data>=0L){
            return  true;
        }else {
            return false;
        }
    }


    boolean isLongDoubleOK(Object res){
        long data=Long.valueOf(String.valueOf(res)).longValue();
        if(data>=0L){
            return  true;
        }else {
            return false;
        }
    }

    boolean isBigLongOK(Object res){
        long data=Long.valueOf(String.valueOf(res)).longValue();
        return true;
    }


    boolean isString(Object res){
        return true;
    }
    boolean isDouble(Object res){
        try{
            double data= (double) res;
            return true;
        }catch (Exception e){
            return false;
        }

    }

    boolean isArrayList(Object res){
        try{
            List list= (List) res;
            return true;
        }catch (Exception e){
            return false;
        }
    }


    boolean isHashSet(Object res){
        return true;
    }

    boolean isPong(Object res){
        if("PONG".equalsIgnoreCase(String.valueOf(res))){
            return true;
        }
        return false;
    }
}
