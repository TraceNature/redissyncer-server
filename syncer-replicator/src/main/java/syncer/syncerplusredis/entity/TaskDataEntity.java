package syncer.syncerplusredis.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.entity.thread.OffSetEntity;
import syncer.syncerplusredis.model.TaskModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/16
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskDataEntity {
    private  TaskModel taskModel;
    private OffSetEntity offSetEntity;
}
