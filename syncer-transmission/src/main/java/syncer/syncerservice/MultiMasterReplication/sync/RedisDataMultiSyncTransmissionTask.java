package syncer.syncerservice.MultiMasterReplication.sync;

import com.alibaba.fastjson.JSON;
import syncer.syncerjedis.Jedis;
import syncer.syncerjedis.JedisPool;
import syncer.syncerjedis.JedisPoolConfig;
import syncer.syncerjedis.Pipeline;
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
import syncer.syncerservice.pool.RedisClient;
import syncer.syncerservice.util.JDRedisClient.RedisMigrator;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.common.Strings;
import syncer.syncerservice.util.jedis.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
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
public class RedisDataMultiSyncTransmissionTask implements Runnable {

    private  String uri;
    private String host;
    private Integer port;
    private String aServerId;
    private String bServerId;
    private String name;
    boolean lastBSelect=false;
    boolean selectStatus=true;
    private Integer serverNumber;

    long timeA=0L;
    long timeB=0L;
//    Set<String> dataSet=new CopyOnWriteArraySet<>();
//    Set<String> dataSetB=new CopyOnWriteArraySet<>();
    public RedisDataMultiSyncTransmissionTask(String uri, String host, Integer port, String aServerId, String bServerId, String name, Integer serverNumber) {
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.aServerId = aServerId;
        this.bServerId = bServerId;
        this.name = name;
        this.serverNumber= serverNumber;
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
            Jedis clientB=pool.getResource();

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


            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=getSelfDataMap().size();
                    int j=getOtherDataMap().size();
                    while (true){

                        if(getSelfDataMap().size()>0){
                            System.out.println("A:"+JSON.toJSONString(getSelfDataMap()));
                        }
                        if(getOtherDataMap().size()>0){
                            System.out.println("B:"+JSON.toJSONString(getOtherDataMap()));
                        }
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

//            Pipeline pipeline=client.pipelined();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=getSelfDataMap().size();
                    int j=getOtherDataMap().size();
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
                    System.out.println("[from "+name+":"+aServerId+bServerId+"]"+Strings.byteToString(command.getCommand())+ JSON.toJSONString(Strings.byteToString(command.getArgs())));

//                    if(selectStatus){
//                        if(getSelfDataMap().size()>0&&getOtherDataMap().size()>0){
//                            byte[][]ndata=new byte[][]{"0".getBytes()};
//                            DefaultCommand selectCommand=new DefaultCommand();
//                            selectCommand.setCommand("SELECT".getBytes());
//                            selectCommand.setArgs(ndata);
//                            String keyA=getMd5(selectCommand,bServerId);
//                            String keyB=getMd5(selectCommand,aServerId);
//                            if(getSelfDataMap().containsKey(keyA)&&getOtherDataMap().containsKey(keyB)){
//                                removeDataMap(getSelfDataMap(),keyA);
//                                removeDataMap(getOtherDataMap(),keyB);
//                                selectStatus=false;
//                            }
//
//                        }
//                    }


                    /**
                     * 非自身辅助key抛弃 并加到A
                     */
                    if(isCircleKey(command,bServerId)){
                        if("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())){
                            command=null;
                            event=null;
                            return;
                        }
                        addDataMap(getSelfDataMap(),Strings.byteToString(command.getArgs())[0]);
                        command=null;
                        event=null;
//                        dataSet.add(Strings.byteToString(command.getArgs())[0]);
                        return;
                    }


                    /**
                     * 若为自身辅助key写道对面 并写到B
                     */
                    if(isCircleKey(command,aServerId)){
                        if("DEL".equalsIgnoreCase(Strings.byteToString(command.getCommand()).trim())){
                            return;
                        }
                        addDataMap(getOtherDataMap(),Strings.byteToString(command.getArgs())[0]);
//                        dataSetB.add(Strings.byteToString(command.getArgs())[0]);
                        try {
                            Object res= getClientB(clientB).sendCommand(ClusterProtocolCommand.builder().raw(command.getCommand()).build(),command.getArgs());
//                            clientB.send(command.getCommand(),command.getArgs());
                            command=null;
                            event=null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }


//                    System.out.println("-------------"+Strings.byteToString(command.getArgs())[0]+isCircle(command,aServerId,bServerId)+aServerId);

                    /**
                     * 非自身key抛弃
                     */
                    if(!isCircleKey(command,bServerId)&&!isCircleKey(command,aServerId)){
                        String md5=getMd5(command,bServerId);
                        if(getSelfDataMap().containsKey(md5)){
                            removeDataMap(getSelfDataMap(),md5);
                            command=null;
                            event=null;
                            return;
                        }

//                        if(dataSet.contains(md5)){
//                            dataSet.remove(md5);
//                            return;
//                        }
                    }

                    /**
                     * 自身key 发送到对面 写到B
                     */
                    if(!isCircleKey(command,bServerId)&&!isCircleKey(command,aServerId)){
                        String md5=getMd5(command,aServerId);
                        if(getOtherDataMap().containsKey(md5)){
                            removeDataMap( getOtherDataMap(),md5);

//                            getOtherDataMap().remove(md5);
                            try {
                                getClientB(clientB).sendCommand(ClusterProtocolCommand.builder().raw(command.getCommand()).build(),command.getArgs());

                                command=null;
                                event=null;

//                                clientB.send(command.getCommand(),command.getArgs());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
//                        if(dataSetB.contains(md5)){
//                            dataSetB.remove(md5);
//                            try {
//                                clientB.sendCommand(ClusterProtocolCommand.builder().raw(command.getCommand()).build(),command.getArgs());
////                                clientB.send(command.getCommand(),command.getArgs());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            return;
//                        }
                    }


//                    System.out.println("[from "+name+":"+aServerId+bServerId+"]"+Strings.byteToString(command.getCommand())+ JSON.toJSONString(Strings.byteToString(command.getArgs())));


                    try {
                        String[]data=new String[]{getMd5(command,bServerId),"1","1"};
//                        System.out.println("++p:"+data[0]);
//
//                        System.out.println("ooo:"+getStringCommand(command));
                        byte[][]ndata=new byte[][]{data[0].getBytes(),data[1].getBytes(),data[2].getBytes()};
//                        pipeline.sendCommand(ClusterProtocolCommand.builder().raw("PSETEX".getBytes()).build(),ndata);
//                        pipeline.sendCommand(ClusterProtocolCommand.builder().raw(command.getCommand()).build(),command.getArgs());
//                        pipeline.sync();
                          getClientA(client).sendCommand(ClusterProtocolCommand.builder().raw("PSETEX".getBytes()).build(),ndata);
                          getClientA(client) .sendCommand(ClusterProtocolCommand.builder().raw(command.getCommand()).build(),command.getArgs());
                          command=null;
                          event=null;
                    } catch (Exception e) {
                        e.printStackTrace();
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
        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://45.40.203.109:20001?authPassword=redistest0102","45.40.203.109",20002,"A239","B240","A239",1));
        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://45.40.203.109:20002?authPassword=redistest0102","45.40.203.109",20001,"A239","B240","B240",2));
//        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.239:20001?authPassword=redistest0102","114.67.100.240",20001,"A239","B240","A239"));
//        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.240:20001?authPassword=redistest0102","114.67.100.239",20001,"A239","B240","B240"));

        threadA.start();
        threadB.start();
    }

    public synchronized static String getMd5(DefaultCommand defaultCommand,String serverId){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("circle-");
        stringBuilder.append(serverId);
        stringBuilder.append("-");
        if(defaultCommand.getArgs()!=null&&defaultCommand.getArgs().length>0){
//            if(Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("SELECT")){
//                stringBuilder.append("SELECT") ;
//            }else if(Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("DEL")){
//                stringBuilder.append("DEL") ;
//            }
//           stringBuilder.append(Strings.byteToString(defaultCommand.getArgs())[0]) ;
            stringBuilder.append(Strings.byteToString(defaultCommand.getCommand())) ;
           stringBuilder.append("-");
        }
        stringBuilder.append(MD5Utils.getMD5(getStringCommand(defaultCommand)));
        return stringBuilder.toString();
    }


    synchronized static  boolean isOtherSelect(DefaultCommand defaultCommand,String bServerId){

        if(defaultCommand.getCommand()!=null&&Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("PSETEX")){
            if(defaultCommand.getArgs()!=null){
                String[] data=Strings.byteToString(defaultCommand.getArgs());
                List<String>dataList= Arrays.asList(data).stream().filter(s-> !StringUtils.isEmpty(s)).collect(Collectors.toList());
//                if(dataList.get(0))
                String key="circle-"+bServerId+"-";
                if(dataList.get(0).startsWith(key.toLowerCase())&&dataList.get(0).toUpperCase().indexOf("SELECT")>0){
                    return true;
                }
            }
        }
        return false;
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

    /**
     * 判断是否是辅助key
     * @param defaultCommand
     * @param serverId
     * @return
     */
    public boolean isCircleKey(DefaultCommand defaultCommand,String serverId){
        if(defaultCommand.getCommand()!=null&&Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("PSETEX")){
            if(defaultCommand.getArgs()!=null){
                String[] data=Strings.byteToString(defaultCommand.getArgs());
                List<String>dataList= Arrays.asList(data).stream().filter(s-> !StringUtils.isEmpty(s)).collect(Collectors.toList());
                String key="circle-"+serverId+"-";
                if(dataList.get(0).startsWith(key)){
                    return true;
                }
            }
        }else if(defaultCommand.getCommand()!=null&&Strings.byteToString(defaultCommand.getCommand()).equalsIgnoreCase("DEL")){
            if(defaultCommand.getArgs()!=null&&defaultCommand.getArgs().length>0){
                String[] data=Strings.byteToString(defaultCommand.getArgs());
                List<String>dataList= Arrays.asList(data).stream().filter(s-> !StringUtils.isEmpty(s)).collect(Collectors.toList());
                String key="circle-"+serverId+"-";
                if(dataList.get(0).startsWith(key)){
                    return true;
                }

            }
        }
        return false;
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

    synchronized Jedis getClientB(Jedis client){
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
}
