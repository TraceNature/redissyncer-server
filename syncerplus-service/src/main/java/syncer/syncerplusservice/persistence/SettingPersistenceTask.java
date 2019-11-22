package syncer.syncerplusservice.persistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusservice.util.file.FileUtils;

import java.util.HashMap;
import java.util.Map;

public class SettingPersistenceTask implements Runnable{

    @Override
    public void run() {
        while (true){
            try {
                FileUtils.flushSettings();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        Map<String, ThreadMsgEntity> aliveThreadHashMap=JSON.parseObject(FileUtils.getText(System.getProperty("user.dir")+ FileUtils.getSettingName()),new TypeReference<HashMap<String,ThreadMsgEntity>>() {});
        System.out.println(aliveThreadHashMap);
        System.out.println(JSON.toJSONString(aliveThreadHashMap));
        System.out.println(System.getProperty("user.dir"));
    }
}
