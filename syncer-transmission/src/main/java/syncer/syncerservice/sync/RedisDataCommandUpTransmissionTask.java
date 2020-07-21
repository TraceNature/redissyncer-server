package syncer.syncerservice.sync;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.config.ThreadPoolConfig;
import syncer.syncerpluscommon.util.file.FileUtils;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.cmd.Command;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.RedisURI;
import syncer.syncerplusredis.event.Event;
import syncer.syncerplusredis.event.EventListener;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.extend.replicator.service.JDRedisReplicator;
import syncer.syncerplusredis.io.RawByteListener;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.replicator.Replicator;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.TaskErrorUtils;
import syncer.syncerservice.util.RedisUrlCheckUtils;
import syncer.syncerservice.util.common.Strings;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhanenqiang
 * @Description 增量命令实时备份AOF
 * @Date 2020/7/20
 */

@Slf4j
@AllArgsConstructor
public class RedisDataCommandUpTransmissionTask implements Runnable{
    private String taskId;
    private String fileAdress;
    private String sourceUri;
    private TaskModel taskModel;
    private boolean status=true;
    private Lock lock=new ReentrantLock();
    private volatile OutputStream out ;
    private volatile AtomicInteger acc = new AtomicInteger(0);
    private Date time;
    private Replicator r;
    static ThreadPoolConfig threadPoolConfig;
    static ThreadPoolTaskExecutor threadPoolTaskExecutor;
    static {
        threadPoolConfig = SpringUtil.getBean(ThreadPoolConfig.class);
        threadPoolTaskExecutor = threadPoolConfig.threadPoolTaskExecutor();
    }

    public RedisDataCommandUpTransmissionTask(TaskModel taskModel) {
        this.taskModel = taskModel;
        this.taskId = taskModel.getId();
        this.fileAdress = taskModel.getFileAddress();
        this.sourceUri = taskModel.getSourceUri();
        this.time=new Date();
        try {

            if(FileUtils.isDirectory(fileAdress)){
                fileAdress+="/"+taskId+".aof";
            }


            this.out=new BufferedOutputStream(new FileOutputStream(new File(fileAdress)));
        }  catch (Exception e) {
            try {
                TaskErrorUtils.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
            } catch (Exception ex) {
                log.warn("任务Id【{}】异常结束任务失败，失败原因【{}】", taskId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("命令备份任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }

        threadPoolTaskExecutor.execute(new sumbitTask());
    }

    @Override
    public void run() {
        try {
            Thread.currentThread().setName(taskId+": "+Thread.currentThread().getName());
            final RawByteListener rawByteListener=rawByteListener();
            RedisURI suri = new RedisURI(sourceUri);
            r= new JDRedisReplicator(suri);

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
                        if (TaskDataManagerUtils.isTaskClose(taskModel.getId())) {
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
                                    log.warn("[{}]增量备份任务线程进入关闭保护状态....",Thread.currentThread().getName());
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
            TaskDataManagerUtils.changeThreadStatus(taskModel.getId(),-1L, TaskStatusType.RUN);

        }catch (Exception e){
            try {
                TaskErrorUtils.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
            } catch (Exception ex) {
                log.warn("任务Id【{}】异常结束任务失败，失败原因【{}】", taskId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }
    }


    /**
     * 刷新缓冲区
     */
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


    RawByteListener rawByteListener(){
        return new RawByteListener(){
            List<byte[]> command=new ArrayList<>();
            boolean status=false;
            @Override
            public void handle(byte... rawBytes) {
                try {
                    String stringdata= Strings.byteToString(rawBytes);

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
                        }else{
                            command.add(rawBytes);
                        }

                    }

                    if (TaskDataManagerUtils.isTaskClose(taskId)) {
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
                                log.warn("[{}]增量备份任务线程进入关闭保护状态....",Thread.currentThread().getName());
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
    }

    /**
     * 磁盘刷新线程
     */
    class sumbitTask implements Runnable{

        @Override
        public void run() {
            Thread.currentThread().setName(taskId+": "+Thread.currentThread().getName());
            while (true){
                sumbit();
                if (TaskDataManagerUtils.isTaskClose(taskModel.getId())) {
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
                            log.warn("[{}]增量备份任务线程进入关闭保护状态....",Thread.currentThread().getName());
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }



}
