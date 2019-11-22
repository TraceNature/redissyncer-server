package syncer.syncerplusservice.filetask.single;

import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.constant.RedisCommandTypeEnum;
import syncer.syncerplusredis.entity.*;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.RedisSyncDataDto;
import syncer.syncerplusredis.entity.thread.EventTypeEntity;
import syncer.syncerplusredis.event.*;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.extend.replicator.listener.ValueDumpIterableEventListener;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.extend.replicator.visitor.ValueDumpIterableRdbVisitor;
import syncer.syncerplusredis.rdb.datatype.DB;
import syncer.syncerplusredis.rdb.dump.datatype.DumpKeyValuePair;
import syncer.syncerplusredis.rdb.iterable.datatype.BatchedKeyValuePair;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusservice.pool.RedisMigrator;
import syncer.syncerplusservice.rdbtask.enums.RedisCommandType;
import syncer.syncerplusservice.rdbtask.single.pipeline.PipelineLock;
import syncer.syncerplusservice.rdbtask.single.pipeline.SendPipelineRdbCommand;
import syncer.syncerplusservice.task.clusterTask.command.ClusterProtocolCommand;
import syncer.syncerplusservice.task.singleTask.pipe.LockPipe;
import syncer.syncerplusservice.task.singleTask.pipe.PipelinedSyncTask;
import syncer.syncerplusservice.util.Jedis.JDJedis;
import syncer.syncerplusservice.util.Jedis.pool.JDJedisClientPool;
import syncer.syncerplusservice.util.RedisUrlUtils;
import syncer.syncerplusservice.util.SyncTaskUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RdbFilePipelineRestoreTask implements Runnable {

    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;

    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }
    private String fileAddress;  //源redis地址
    private String targetUri;  //目标redis地址
    private boolean status = true;
    private String threadName; //线程名称
    private RedisSyncDataDto syncDataDto;
    private SendPipelineRdbCommand sendDefaultCommand = new SendPipelineRdbCommand();
    private RedisInfo info;
    private String taskId;
    private boolean syncStatus = true;


    Pipeline pipelined = null;

    private Lock lock = new ReentrantLock();
    @Getter
    @Setter
    private String dbindex="-1";
    private final AtomicLong dbNum = new AtomicLong(0);
    //判断增量是否可写
    private final AtomicBoolean commandDbStatus=new AtomicBoolean(true);
    private LockPipe lockPipe = new LockPipe();
    private SyncTaskEntity taskEntity = new SyncTaskEntity();

    private Date time;
    private int  batchSize;
    private FileType type;


    public RdbFilePipelineRestoreTask(RedisSyncDataDto syncDataDto, RedisInfo info, String taskId,int batchSize) {

        this.syncDataDto = syncDataDto;
        this.fileAddress = syncDataDto.getFileAddress();
        this.targetUri = syncDataDto.getTargetUri();
        this.threadName = syncDataDto.getTaskName();
        this.info = info;
        this.taskId = taskId;
        this.batchSize=batchSize;
        this.type=syncDataDto.getFileType();
    }



    @Override
    public void run() {
        if(batchSize==0){
            batchSize=1000;
        }

        //设线程名称
        Thread.currentThread().setName(threadName);
        System.out.println("batchSize:"+batchSize);
        try {
            RedisURI turi = new RedisURI(targetUri);
            JDJedisClientPool targetJedisClientPool = RedisUrlUtils.getJDJedisClients(syncDataDto, turi);

            final JDJedis targetJedisplus = targetJedisClientPool.getResource();
            if (pipelined == null) {
                pipelined = targetJedisplus.pipelined();
            }
            PipelineLock pipelineLock=new PipelineLock(pipelined,taskEntity,taskId,targetJedisplus,targetJedisClientPool);

            final Replicator r = RedisMigrator.newBacthedCommandDress(new JDRedisReplicator(null, type,fileAddress,Configuration.defaultSetting(),taskId));

//            final Replicator r = RedisMigrator.newBacthedCommandDress(replicator);
            r.setRdbVisitor(new ValueDumpIterableRdbVisitor(r, info.getRdbVersion()));
            r.addEventListener(new ValueDumpIterableEventListener(new EventListener() {
                @Override
                public void onEvent(Replicator replicator, Event event) {


                    lockPipe.syncpipe(pipelineLock, taskEntity, batchSize, true,null,turi);

                    if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {

                        try {
                            r.close();
//                            pools.close();
                            if (status) {
                                Thread.currentThread().interrupt();
                                status = false;
                                System.out.println(" 线程正准备关闭..." + Thread.currentThread().getName());
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }


                    /**
                     * 管道的形式
                     */
                    if (syncStatus) {
//                        threadPoolTaskExecutor.submit(new PipelinedSyncTask(pipelined, taskEntity,lockPipe));
                        threadPoolTaskExecutor.submit(new PipelinedSyncTask(pipelineLock, taskEntity,lockPipe,taskId,null,turi));
                        syncStatus = false;
                    }

                    if(event instanceof PreCommandSyncEvent){
                        time=new Date();
                        log.warn("【{}】 :全量同步启动",taskId);
                    }

                    if (event instanceof PreRdbSyncEvent) {
                        time=new Date();
                        log.warn("【{}】 :全量同步启动",taskId);
                    }




                    if (event instanceof PostRdbSyncEvent) {
                        long set=new Date().getTime()-time.getTime();
                        if(set/1000==0){
                            SyncTaskUtils.editTaskMsg(taskId,"全量同步结束 时间(ms)："+set);
                            log.warn("【{}】 :全量同步结束 时间：{}(ms)",taskId,set);
                        }else {
                            set=set/1000;
                            SyncTaskUtils.editTaskMsg(taskId,"全量同步结束 时间(s)："+set);
                            log.warn("【{}】 :全量同步结束 时间：{}(s)",taskId,set);
                        }

                        try {
                            Map<String, String> msg = SyncTaskUtils.stopCreateThread(Arrays.asList(taskId));
                        } catch (TaskMsgException e) {
                            e.printStackTrace();
                        }
                    }

                    if (event instanceof PostCommandSyncEvent) {
                        long set=new Date().getTime()-time.getTime();
                        if(set/1000==0){
                            SyncTaskUtils.editTaskMsg(taskId,"全量同步结束 时间(ms)："+set);
                            log.warn("【{}】 :全量同步结束 时间：{}(ms)",taskId,set);
                        }else {
                            set=set/1000;
                            SyncTaskUtils.editTaskMsg(taskId,"全量同步结束 时间(s)："+set);
                            log.warn("【{}】 :全量同步结束 时间：{}(s)",taskId,set);
                        }

                        try {
                            Map<String, String> msg = SyncTaskUtils.stopCreateThread(Arrays.asList(taskId));
                        } catch (TaskMsgException e) {
                            e.printStackTrace();
                        }
                    }

                    if (event instanceof BatchedKeyValuePair<?, ?>) {

                        BatchedKeyValuePair event1 = (BatchedKeyValuePair) event;


                        DB db = event1.getDb();
                        Long ms;
                        if (event1.getExpiredMs() == null) {
                            ms = 0L;
                        } else {
//                            ms = event1.getExpiredMs();
                            ms = event1.getExpiredMs() - System.currentTimeMillis();
                            if(ms<0L){
                                return;
                            }
                        }
                        if (event1.getValue() != null) {


                            int dbbnum = (int) db.getDbNumber();
                            if (null != syncDataDto.getDbMapper() && syncDataDto.getDbMapper().size() > 0) {
                                if (syncDataDto.getDbMapper().containsKey((int) db.getDbNumber())) {

                                    dbbnum = syncDataDto.getDbMapper().get((int) db.getDbNumber());
                                } else {
                                    return;
                                }
                            }

                            if (lockPipe.getDbNum() != dbbnum) {
                                pipelined.select(dbbnum);

                                EventEntity eventEntity=new EventEntity("SELECT".getBytes(),ms,null, EventTypeEntity.USE, RedisCommandTypeEnum.STRING);
                                taskEntity.addKey(eventEntity);

                                lockPipe.setDbNum(dbbnum);
                                taskEntity.add();

                            }


                            try {

                                sendDefaultCommand.sendSingleCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,pipelineLock,new String((byte[]) event1.getKey()),syncDataDto.getTargetRedisVersion(),taskEntity);

//                                sendDefaultCommand.sendSingleCommand(ms,RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()),event,pipelined,new String((byte[]) event1.getKey()),syncDataDto.getRedisVersion(),taskEntity);
//                                threadPoolTaskExecutor.submit(new SendRdbCommand(ms, RedisCommandType.getRedisCommandTypeEnum(event1.getValueRdbType()), event, RedisCommandType.getJDJedis(targetJedisClientPool, event, syncDataDto.getDbNum()), new String((byte[]) event1.getKey()), syncDataDto.getRedisVersion()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    }
                    if (event instanceof DumpKeyValuePair) {
//                        System.out.println(r.getConfiguration().getReplId()+":"+r.getConfiguration().getReplOffset());

                        DumpKeyValuePair valuePair = (DumpKeyValuePair) event;
                        if (valuePair.getValue() != null) {
                            Long ms;
                            if (valuePair.getExpiredMs() == null) {
                                ms = 0L;
                            } else {

                                ms = valuePair.getExpiredMs() - System.currentTimeMillis();
                                if(ms<0L){
                                    return;
                                }
                            }

                            DB db = valuePair.getDb();

                            int dbbnum = (int) db.getDbNumber();
                            if (null != syncDataDto.getDbMapper() && syncDataDto.getDbMapper().size() > 0) {
                                if (syncDataDto.getDbMapper().containsKey((int) db.getDbNumber())) {

                                    dbbnum = syncDataDto.getDbMapper().get((int) db.getDbNumber());
                                } else {
                                    return;
                                }
                            }

                            try {

                                if (lockPipe.getDbNum() != dbbnum) {
                                    pipelined.select(dbbnum);
                                    lockPipe.setDbNum(dbbnum);
                                    EventEntity eventEntity=new EventEntity("SELECT".getBytes(),ms,null, EventTypeEntity.USE,RedisCommandTypeEnum.STRING);
                                    taskEntity.addKey(eventEntity);
                                    taskEntity.add();
                                }

                                sendDefaultCommand.sendSingleCommand(ms,RedisCommandTypeEnum.DUMP,event,pipelineLock,new String((byte[]) valuePair.getKey()),syncDataDto.getTargetRedisVersion(),taskEntity);


//                                threadPoolTaskExecutor.submit(new SendRdbCommand(ms, RedisCommandTypeEnum.DUMP, event, RedisCommandType.getJDJedis(targetJedisClientPool, event, syncDataDto.getDbNum()), new String((byte[]) valuePair.getKey()), syncDataDto.getRedisVersion()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }



                    if (event instanceof DefaultCommand) {
//                      TaskMsgUtils.getThreadMsgEntity(taskId).getOffsetMap().put(taskId,baseOffSet);
                        DefaultCommand dc = (DefaultCommand) event;


                        if(Arrays.equals(dc.getCommand(),"SELECT".getBytes())){
                            int commDbNum= Integer.parseInt(new String(dc.getArgs()[0]));


                            if(syncDataDto.getDbMapper()==null||syncDataDto.getDbMapper().size()==0){
                                dbNum.set(commDbNum);
                                commandDbStatus.set(true);
                            }else {
                                if(syncDataDto.getDbMapper().containsKey(commDbNum)){
                                    byte[][]dbData=dc.getArgs();
                                    dbData[0]=String.valueOf(syncDataDto.getDbMapper().get(commDbNum)).getBytes();
                                    dc.setArgs(dbData);
                                    dbNum.set(syncDataDto.getDbMapper().get(commDbNum));
                                    commandDbStatus.set(true);
                                }else {
                                    commandDbStatus.set(false);
                                    return ;
                                }

                            }

                        }else {

                            if(!commandDbStatus.get()&&!Arrays.equals(dc.getCommand(),"FLUSHALL".getBytes())){
                                return;
                            }

                            EventEntity eventEntity=new EventEntity(new DB(dbNum.get()),EventTypeEntity.USE,RedisCommandTypeEnum.COMMAND,dc);
//                            EventEntity eventEntity=new EventEntity(dc.getArgs()[0],new DB(dbNum.get()),EventTypeEntity.USE,RedisCommandTypeEnum.COMMAND);
                            taskEntity.addKey(eventEntity);

                        }
                        if(!commandDbStatus.get()&&!Arrays.equals(dc.getCommand(),"FLUSHALL".getBytes())){
                            return;
                        }

                        taskEntity.add();
                        pipelineLock.sendCommand(new ClusterProtocolCommand(dc.getCommand()), dc.getArgs());


                    }


                }
            }));

            r.open(taskId);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (EOFException ex) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),ex.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ex.getMessage());
        } catch (NoRouteToHostException p) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),p.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, p.getMessage());
        } catch (ConnectException cx) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),cx.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, cx.getMessage());
        }catch (AssertionError er){
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),er.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, er.getMessage());
        }catch (JedisConnectionException ty){
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),ty.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ty.getMessage());
        }catch (SocketException ii){
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),ii.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ii.getMessage());
        }

        catch (IOException et) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),et.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, et.getMessage());
        } catch (IncrementException et) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),et.getMessage());
            } catch (TaskMsgException e) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, et.getMessage());
        }catch (Exception e){
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId),e.getMessage());
            } catch (TaskMsgException ep) {
                e.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        RedisURI redis=new RedisURI("redis:///path/to/dump.rdb?filetype=onlinerdb");
        System.out.println(redis.getFileType());

    }


    /**
     * 从网络Url中下载文件
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static void  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
//        File saveDir = new File(savePath);
//        if(!saveDir.exists()){
//            saveDir.mkdir();
//        }
//        File file = new File(saveDir+File.separator+fileName);
//        FileOutputStream fos = new FileOutputStream(file);

        System.out.println(getData);
//        fos.write(getData);
//        if(fos!=null){
//            fos.close();
//        }
//        if(inputStream!=null){
//            inputStream.close();
//        }


        System.out.println("info:"+url+" download success");

    }


    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {


            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
