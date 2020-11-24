package syncer.syncerservice.sync;

import syncer.syncerpluscommon.util.ThreadPoolUtils;
import syncer.syncerpluscommon.util.file.FileUtils;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.MultiMasterReplication.sync.RedisDataMultiSyncTransmissionTask;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description redis双向同步任务
 * @Date 2020/9/15
 */
public class RedisDataBidirectionalSynchronizationtTask implements Runnable{

    private ParentMultiTaskModel parentMultiTaskModel;

    @Override
    public void run() {
        String md5A="A239";
        String md5B="B240";

        ThreadPoolUtils.exec(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.239:20001?authPassword=redistest0102", "114.67.100.240",20001,md5A,md5B,"A239",6));
        ThreadPoolUtils.exec(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.240:20001?authPassword=redistest0102","114.67.100.239",20001,md5A,md5B,"B240",6));

//        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.239:20001?authPassword=redistest0102", "114.67.100.240",20001,md5A,md5B,"A239",6));
//        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://114.67.100.240:20001?authPassword=redistest0102","114.67.100.239",20001,md5A,md5B,"B240",6));

//        Thread threadA=new Thread(new RedisDataMultiSyncTransmissionTask("redis://10.0.1.45:20001?authPassword=redistest0102","10.0.1.46",20001,md5A,md5B,"A239"));
//        Thread threadB=new Thread(new RedisDataMultiSyncTransmissionTask("redis://10.0.1.46:20001?authPassword=redistest0102","10.0.1.45",20001,md5A,md5B,"B240"));


        String settingPath = System.getProperty("user.dir") + FileUtils.getSettingName();
        if(FileUtils.existsFile(settingPath)){
//            ConcurrentHashMap<String,ThreadMsgEntity> data=JSON.parseObject(FileUtils.getText(System.getProperty("user.dir")+ FileUtils.getSettingName()),new TypeReference<ConcurrentHashMap<String, ThreadMsgEntity>>() {});
            ConcurrentHashMap<String, ThreadMsgEntity> data= null;
            try {
                data = (ConcurrentHashMap<String, ThreadMsgEntity>) FileUtils.FileInputToObject(settingPath);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, ThreadMsgEntity> entry:data.entrySet()
                 ) {
                ThreadMsgEntity msgEntity=entry.getValue();
                msgEntity.setRList(new ArrayList<>());
                msgEntity.setStatus(ThreadStatusEnum.STOP);
                data.put(entry.getKey(),msgEntity);
            }
            if(data==null){
                data=new  ConcurrentHashMap<String,ThreadMsgEntity>(10);
            }
            TaskMsgUtils.setAliveThreadHashMap(data);

        }
    }
}
