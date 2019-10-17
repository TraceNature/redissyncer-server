package com.i1314i.syncerplusredis.entity.dto.task;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter

public class TaskMsgDto {

    @NotEmpty(message = "taskids不能为空")
    private List<String> taskids;

//    @NotEmpty(message = "taskid不能为空")
//    private String taskid;

}
