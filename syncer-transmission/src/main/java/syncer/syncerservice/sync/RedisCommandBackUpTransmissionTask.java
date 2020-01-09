package syncer.syncerservice.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.Command;
import syncer.syncerplusredis.cmd.CommandName;
import syncer.syncerplusredis.cmd.impl.PingCommand;
import syncer.syncerplusredis.cmd.parser.PingParser;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.io.RawByteListener;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.common.Strings;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/2
 */
@Slf4j
public class RedisCommandBackUpTransmissionTask implements Runnable {
    private String taskId;
    private String fileAdress;
    private String sourceUri;
    private boolean status=true;
    private Lock lock=new ReentrantLock();
    private volatile OutputStream out ;
    private volatile  AtomicInteger acc = new AtomicInteger(0);
    private Date time;
    private Replicator r;
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;
    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    public RedisCommandBackUpTransmissionTask(String taskId, String fileAdress, String sourceUri) {
        this.taskId = taskId;
        this.fileAdress = fileAdress;
        this.sourceUri = sourceUri;
        this.time=new Date();
        try {
            this.out=new BufferedOutputStream(new FileOutputStream(new File(fileAdress)));
        }  catch (Exception e) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), e.getMessage());
            } catch (TaskMsgException ex) {
                log.warn("任务Id【{}】异常结束任务失败，失败原因【{}】", taskId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }

        threadPoolTaskExecutor.execute(new sumbitTask());
    }

    @Override
    public void run() {

        try {
        final RawByteListener rawByteListener = new RawByteListener() {
            List<byte[]>command=new ArrayList<>();
            boolean status=false;
            @Override
            public void handle(byte... rawBytes) {
                try {
                    String stringdata=Strings.byteToString(rawBytes);

                    if(command.size()>0&&stringdata.startsWith("*")){
                        if(command.size()>0&&!status){

                            for (byte[]raw:command
                                 ) {
                                out.write(raw);
                            }
                            command.clear();
                            status=false;
                        }else {
                            command.clear();
                            status=false;
                        }
                        command.add(rawBytes);
                    }else {
                        if("PING".equalsIgnoreCase(stringdata)){
                            status=true;
                        }
                        command.add(rawBytes);
                    }

                    if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {
                        //判断任务是否关闭
                        try {
                            try {
                                out.flush();
                                out.close();
                                r.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (status) {
                                Thread.currentThread().interrupt();
                                status = false;
                                System.out.println(" 线程正准备关闭..." + Thread.currentThread().getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }

//                    System.out.println("=---"+Strings.byteToString(rawBytes));
//                    out.write(rawBytes);
//                    out.flush();
                } catch (IOException ignore) {
                }
            }
        };
            RedisURI suri = new RedisURI(sourceUri);
        //save 1000 records commands
            r= new JDRedisReplicator(suri);

//            r.addCommandParser(CommandName.name("PING"), new PingParser());

            //只增量相关代码


                String[] data = RedisUrlCheckUtils.selectSyncerBuffer(sourceUri, "endbuf");
                long offsetNum = 0L;
                try {
                    offsetNum = Long.parseLong(data[0]);
                    offsetNum -= 1;
                    //offsetNum -= 1;
                } catch (Exception e) {

                }
                if (offsetNum != 0L && !StringUtils.isEmpty(data[1])) {
                    r.getConfiguration().setReplOffset(offsetNum);
                    r.getConfiguration().setReplId(data[1]);
                }



            r.addEventListener(new EventListener() {
            @Override
            public void onEvent(Replicator replicator, Event event) {


                if (event instanceof PreCommandSyncEvent) {
                    r.addRawByteListener(rawByteListener);
                }

//                if(event instanceof PingCommand){
//                    return;
//                }


                if (event instanceof Command) {

                    if (acc.incrementAndGet() >= 1000) {
                        sumbit();
                    }

                    if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {
                        //判断任务是否关闭
                        try {
                            try {
                                out.close();
                                replicator.close();
                                r.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (status) {
                                Thread.currentThread().interrupt();
                                status = false;
                                System.out.println(" 线程正准备关闭..." + Thread.currentThread().getName());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }


                }
            }
        });

        r.open(taskId);

        } catch (Exception e) {
            try {
                Map<String, String> msg = SyncTaskUtils.brokenCreateThread(Arrays.asList(taskId), e.getMessage());
            } catch (TaskMsgException ex) {
                log.warn("任务Id【{}】异常结束任务失败，失败原因【{}】", taskId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }
    }

    void sumbit(){
        lock.lock();
        try {

            out.flush();
            acc.set(0);
            time=new Date();
        }catch (Exception e){
            e.printStackTrace();
        } finally{
            lock.unlock();
        }
    }

    class sumbitTask implements Runnable{

        @Override
        public void run() {
            while (true){
//                if(System.currentTimeMillis()-time.getTime()>20000&&acc.get()>0){
//
//                    sumbit();
//                }

                sumbit();
                if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {
                    //判断任务是否关闭
                    try {
                        try {
                            out.flush();

                            out.close();
                            r.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (status) {
                            Thread.currentThread().interrupt();
                            status = false;
                            System.out.println(" 线程正准备关闭..." + Thread.currentThread().getName());
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }


//                if (SyncTaskUtils.doThreadisCloseCheckTask(taskId)) {
//                    //判断任务是否关闭
//                    try {
//                        sumbit();
//                        Thread.currentThread().interrupt();
//
//                        break;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    return;
//                }


                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
