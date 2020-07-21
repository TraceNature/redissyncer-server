package syncer.syncerservice.MultiMasterReplication.sync;

import com.alibaba.fastjson.JSON;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.JedisPool;
import syncer.syncerjedis.JedisPoolConfig;
import syncer.syncerpluscommon.util.md5.MD5Utils;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.replicator.RedisReplicator;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerservice.cmd.ClusterProtocolCommand;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 双活
 * @Date 2020/3/25
 */
public class RedisDataMultiSyncTransmissionType2Task implements Runnable {

    private  String uri;
    private String host;
    private Integer port;
    private Integer serverNumber;
    private String name;
    boolean lastBSelect=false;

    boolean selectStatus=true;
    long timeA=0L;
    long timeB=0L;
    Set<String> dataSet=new CopyOnWriteArraySet<>();
    Set<String> dataSetB=new CopyOnWriteArraySet<>();
    public RedisDataMultiSyncTransmissionType2Task(String uri, String host, Integer port, Integer serverNumber,String name) {
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.serverNumber = serverNumber;
        this.name = name;
    }

    /**
     * 目标Redis类型
     */

    private boolean status = true;

    @Override
    public void run() {
        try {

         Replicator replicator =RedisMigrator.newBacthedCommandDress(new RedisReplicator(uri));
         Integer dbNum=0;
        //注册增量命令解析器
        final Replicator replicationHandler = replicator;
//        RedisClient client=new RedisClient(host,port);

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(5);
            config.setMaxIdle(3);
            config.setMinIdle(2);
            //当池内没有返回对象时，最大等待时间
            config.setMaxWaitMillis(10000);
            config.setTimeBetweenEvictionRunsMillis(30000);
            config.setTestOnReturn(true);
            config.setBlockWhenExhausted(true);
            config.setTestOnBorrow(true);

            JedisPool pool = new JedisPool(config, host, port, 100000);
            Jedis client=pool.getResource();
//            client.auth("redistest0102");
//        RedisClient clientB=new RedisClient(host,port);
            Jedis clientB=pool.getResource();
//            clientB.auth("redistest0102");


            String[] data = RedisUrlCheckUtils.selectSyncerBuffer(uri, SyncTypeUtils.getOffsetPlace(999).getOffsetPlace());
            long offsetNum = 0L;
            try {
                offsetNum = Long.parseLong(data[0]);
                offsetNum -= 1;
                //offsetNum -= 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (offsetNum != 0L && !org.springframework.util.StringUtils.isEmpty(data[1])) {
                replicationHandler.getConfiguration().setReplOffset(offsetNum);
                replicationHandler.getConfiguration().setReplId(data[1]);
            }

            /**
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=dataMap.size();
                    int j=dataMapB.size();
                    while (true){

                        if(dataMap.size()>0){


                            System.out.println(JSON.toJSONString(dataMap));

                        }

                        if(dataMapB.size()>0){
                            System.out.println(JSON.toJSONString(dataMapB));

                        }


                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
**/
//            Pipeline pipeline=client.pipelined();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=SysncData.dataMap.size();
                    int j=SysncData.dataMapB.size();
                    while (true){
                        if(System.currentTimeMillis()-timeA>280000){
                            getClientA(client).ping();
                        }
                        if(System.currentTimeMillis()-timeB>280000){
                            getClientB(clientB).ping();
                        }


                        try {
                            Thread.sleep(300000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        replicationHandler.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {
                //增量同步开始

                if(event instanceof PreCommandSyncEvent){
                    System.out.println(name+"增量开始...");
                }
                //命令解析器
                if (event instanceof DefaultCommand) {


                    DefaultCommand command= (DefaultCommand) event;

                    System.out.println(getStringCommand(command));
                    if(isDieOutKey(command)){
                        command=null;
                        event=null;
                        return;
                    }else {
                        System.out.println("[from "+name+":"+serverNumber+"]"+Strings.byteToString(command.getCommand())+ JSON.toJSONString(Strings.byteToString(command.getArgs())));
                        System.out.println("A:-"+JSON.toJSONString(SysncData.dataMap));
                        System.out.println("B:-"+JSON.toJSONString(SysncData.dataMapB));
                        Object res= getClientB(clientB).sendCommand(ClusterProtocolCommand.builder().raw(command.getCommand()).build(),command.getArgs());
                        command=null;
                        event=null;
                    }


                }
            }
        });
         replicationHandler.open();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }


    }



    public static void main(String[] args) throws IOException, IncrementException, URISyntaxException {
        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionType2Task("redis://45.40.203.109:20001?authPassword=redistest0102","45.40.203.109",20002,1,"A20001"));
        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionType2Task("redis://45.40.203.109:20002?authPassword=redistest0102","45.40.203.109",20001,2,"B20002"));
        threadA.start();
        threadB.start();
    }





    static synchronized String getStringCommand(DefaultCommand defaultCommand){
        StringBuilder stringBuilder=new StringBuilder();

        if(defaultCommand.getCommand()!=null){
            stringBuilder.append(Strings.byteToString(defaultCommand.getCommand()).toUpperCase());
        }
        if(defaultCommand.getArgs()!=null&&defaultCommand.getArgs().length>0){
            String[] data=Strings.byteToString(defaultCommand.getArgs());
            for (String str:data
                 ) {
                if(StringUtils.isEmpty(str)){
                    continue;
                }

                stringBuilder.append(str);
            }
        }
        return replace(stringBuilder.toString());
    }



    public static String replace(String str) {
        String destination = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            destination = m.replaceAll("");
        }
        return destination;
    }




    static void addDataMap(Map<String,AtomicLong>map,String circleKey){
        if(map.containsKey(circleKey)){
            map.get(circleKey).incrementAndGet();
        }else{
            map.put(circleKey,new AtomicLong(1));
        }
    }

    static void removeDataMap(Map<String,AtomicLong>map,String circleKey){
        if(map.containsKey(circleKey)){
            AtomicLong data=map.get(circleKey);
            if(data.get()<=1){
                map.remove(circleKey);
            }else {
                data.decrementAndGet();
            }
        }
    }

    private  String getUri(String address,String password){
        StringBuilder stringHead = new StringBuilder("redis://");
        //如果截取出空字符串直接跳过
        if (address != null && address.length() > 0) {
            stringHead.append(address);
            //判断密码是否为空如果为空直接跳过
            if (password != null && password.length() > 0) {
                stringHead.append("?authPassword=");
                stringHead.append(password);
            }

            return stringHead.toString();
        }
        return null;
    }


    synchronized Jedis getClientA(Jedis client){
        timeA=System.currentTimeMillis();
        return client;
    }

    synchronized Jedis  getClientB(Jedis client){
        timeB=System.currentTimeMillis();
        return client;
    }


    private Map<String, AtomicLong> getSelfDataMap(){
        if(serverNumber==1){
            return SysncData.dataMap;
        }else {
            return SysncData.dataMapB;
        }
    }

    private Map<String, AtomicLong> getOtherDataMap(){
        if(serverNumber==2){
            return SysncData.dataMap;
        }else {
            return SysncData.dataMapB;
        }
    }

    private boolean isDieOutKey(DefaultCommand defaultCommand){
        String command=getStringCommand(defaultCommand);
//        String md5Key=MD5Utils.getMD5(command);
        String md5Key=command;
        if(getOtherDataMap().containsKey(md5Key)){
            removeDataMap(getOtherDataMap(),md5Key);
            return true;
        }
        addDataMap(getSelfDataMap(),md5Key);
        return false;
    }
}
