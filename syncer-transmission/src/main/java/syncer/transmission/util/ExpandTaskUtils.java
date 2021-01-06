package syncer.transmission.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.ExpandTaskModel;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.sql.SqlOPUtils;

/**
 * @author zhanenqiang
 * @Description 扩展字段工具
 * @Date 2020/8/24
 */
@Slf4j
public class ExpandTaskUtils {
    public static synchronized void  loadingExpandTaskData(TaskModel taskModel, TaskDataEntity dataEntity){
        ExpandTaskModel expand=null;
        try {
            expand= JSON.parseObject(taskModel.getExpandJson(), ExpandTaskModel.class);
            expand.setBrokenReason("");
            taskModel.setExpandJson(JSON.toJSONString(expand));
            SqlOPUtils.updateExpandTaskModelById(taskModel.getId(), JSON.toJSONString(dataEntity.getExpandTaskModel()));
        }catch (Exception e){

            expand=new ExpandTaskModel();
            taskModel.setExpandJson(JSON.toJSONString(expand));
            log.error("[{}]任务扩展JSON逆序列化失败,重新置零",taskModel.getId());
            e.printStackTrace();

        }
        dataEntity.setExpandTaskModel(expand);
    }
}
