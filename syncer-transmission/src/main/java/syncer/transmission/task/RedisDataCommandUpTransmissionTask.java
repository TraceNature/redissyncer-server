package syncer.transmission.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.util.TemplateUtils;
import syncer.common.util.ThreadPoolUtils;
import syncer.common.util.file.FileUtils;
import syncer.replica.cmd.Command;
import syncer.replica.cmd.impl.PingCommand;
import syncer.replica.entity.RedisURI;
import syncer.replica.entity.TaskStatusType;
import syncer.replica.event.Event;
import syncer.replica.event.PreCommandSyncEvent;
import syncer.replica.listener.EventListener;
import syncer.replica.listener.RawByteListener;
import syncer.replica.replication.RedisReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.objectutil.Strings;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.manger.DefaultSyncerStatusManger;
import syncer.transmission.util.redis.RedisReplIdCheck;
import syncer.transmission.util.redis.RedisUrlCheck;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static syncer.replica.constant.CMD.PING;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/18
 */
@Slf4j
public class RedisDataCommandUpTransmissionTask implements Runnable {
    private String taskId;
    private String fileAdress;
    private String sourceUri;
    private TaskModel taskModel;
    private boolean status=true;
    private Lock lock=new ReentrantLock();
    private volatile OutputStream out ;
    private volatile AtomicInteger acc = new AtomicInteger(0);
    private Date time;
    private boolean taskStatus=true;
    private Replication replication;
    RedisReplIdCheck check=new RedisReplIdCheck();
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
                DefaultSyncerStatusManger.brokenStatusAndLog(e,this.getClass(),taskModel.getId());
            } catch (Exception ex) {
                log.warn("任务Id【{}】异常结束任务失败，失败原因【{}】", taskId, e.getMessage());
                ex.printStackTrace();
            }
            log.warn("命令备份任务Id【{}】异常停止，停止原因【{}】", taskId, e.getMessage());
        }

        ThreadPoolUtils.exec(new sumbitTask());
    }

    @Override
    public void run() {
        try{
            Thread.currentThread().setName(taskId+": "+Thread.currentThread().getName());
            final RawByteListener rawByteListener=rawByteListener();
            RedisURI suri = new RedisURI(sourceUri);
            replication =new RedisReplication(suri);

            String[] data = check.selectSyncerBuffer(sourceUri, "endbuf");
            long offsetNum = 0L;
            try {
                offsetNum = Long.parseLong(data[0]);
                offsetNum -= 1;
                //offsetNum -= 1;
            } catch (Exception e) {

            }
            replication.getConfiguration().setTaskId(taskId);

            if (offsetNum != 0L && !StringUtils.isEmpty(data[1])) {
                replication.getConfiguration().setReplOffset(offsetNum);
                replication.getConfiguration().setReplId(data[1]);
            }

            replication.addEventListener(new EventListener() {
                @Override
                public void onEvent(Replication replicator, Event event) {
                    if(taskStatus){
                        try {
                            taskStatus=false;
                            DefaultSyncerStatusManger.changeThreadStatus(taskModel.getId(),-1L, TaskStatusType.COMMANDRUNING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    if (event instanceof PreCommandSyncEvent) {
                        replication.addRawByteListener(rawByteListener);
                    }

                if(event instanceof PingCommand){
                    return;
                }


                    if (event instanceof Command) {
                        if (acc.incrementAndGet() >= 1000) {
                            sumbit();
                        }
                        if (DefaultSyncerStatusManger.isTaskClose(taskModel.getId())) {
                            //判断任务是否关闭
                            try {
                                try {
                                    out.close();
                                    replicator.close();
                                    replication.close();
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
            DefaultSyncerStatusManger.changeThreadStatus(taskModel.getId(),-1L, TaskStatusType.RUN);
            replication.open();
        }catch (Exception e){

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
                        if(PING.equalsIgnoreCase(stringdata)){
                            status=true;
                        }else{
                            command.add(rawBytes);
                        }

                    }

                    if (SingleTaskDataManagerUtils.isTaskClose(taskId)) {
                        //判断任务是否关闭
                        try {
                            try {
                                out.flush();
                                out.close();
                                replication.close();
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
                if (DefaultSyncerStatusManger.isTaskClose(taskModel.getId())) {
                    //判断任务是否关闭
                    try {
                        try {
                            out.flush();
                            out.close();
                            replication.close();
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