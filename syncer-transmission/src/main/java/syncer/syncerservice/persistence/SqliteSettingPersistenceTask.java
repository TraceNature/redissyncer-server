package syncer.syncerservice.persistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import syncer.syncerplusredis.entity.TaskDataEntity;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerpluscommon.util.file.FileUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description sqlite持久化
 * @Date 2020/4/1
 */
public class SqliteSettingPersistenceTask  implements Runnable{

    @Override
    public void run() {


        while (true){
            try {
                Map<String, TaskDataEntity> aliveThreadHashMap =TaskDataManagerUtils.getAliveThreadHashMap();

                for (Map.Entry<String, TaskDataEntity> data:aliveThreadHashMap.entrySet()){


                    if(!(data.getValue().getRdbKeyCount().get()==0L
                            &&data.getValue().getAllKeyCount().get()==0L
                            &&data.getValue().getRealKeyCount().get()==0L)){
                            SqliteOPUtils.updateKeyCountById(data.getKey(),data.getValue().getRdbKeyCount().get(),data.getValue().getAllKeyCount().get(),data.getValue().getRealKeyCount().get());
                    }


                    /**
                     * todo
                     * null异常
                     */
                    if(null!=data.getValue().getOffSetEntity().getReplOffset()&&data.getValue().getOffSetEntity().getReplOffset().get()>-1L){
                        TaskModel sqliteTaskModel=SqliteOPUtils.findTaskById(data.getKey());

                        if(null!=sqliteTaskModel&&null!=sqliteTaskModel.getOffset()&&!sqliteTaskModel.getOffset().equals(data.getValue().getOffSetEntity().getReplOffset().get())){
                            SqliteOPUtils.updateOffset(data.getKey(),data.getValue().getOffSetEntity().getReplOffset().get());
                        }
                    }
                }
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        Map<String, ThreadMsgEntity> aliveThreadHashMap= JSON.parseObject(FileUtils.getText(System.getProperty("user.dir")+ FileUtils.getSettingName()),new TypeReference<HashMap<String,ThreadMsgEntity>>() {});
        System.out.println(aliveThreadHashMap);
        System.out.println(JSON.toJSONString(aliveThreadHashMap));
        System.out.println(System.getProperty("user.dir"));
    }
}
