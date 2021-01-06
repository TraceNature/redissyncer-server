package syncer.transmission.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.replication.Replication;
import syncer.transmission.model.ExpandTaskModel;
import syncer.transmission.model.TaskModel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class TaskDataEntity {
    private TaskModel taskModel;
    private OffSetEntity offSetEntity;
    private Replication replication;
    @Builder.Default
    private ExpandTaskModel expandTaskModel=new ExpandTaskModel();


    @Builder.Default
    private AtomicLong rdbKeyCount=new AtomicLong(0);

    @Builder.Default
    private AtomicLong allKeyCount=new AtomicLong(0);

    @Builder.Default
    private AtomicLong realKeyCount=new AtomicLong(0);

    @Builder.Default
    private AtomicInteger abandonKeyCount=new AtomicInteger(0);


    /**
     * 被抛弃key阈值
     */
    @Builder.Default
    private AtomicLong errorNums = new AtomicLong(0L);




}
