package syncerservice.syncerplusredis.entity.dto.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter

public class TaskStartMsgDto {

//    @NotEmpty(message = "taskids不能为空")
//    private List<String> taskids;

    @NotEmpty(message = "taskid不能为空")
    private String taskid;
    @Builder.Default
    private boolean afresh=true;
}
