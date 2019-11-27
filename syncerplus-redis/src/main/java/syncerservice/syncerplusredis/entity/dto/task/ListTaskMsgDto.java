package syncerservice.syncerplusredis.entity.dto.task;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class ListTaskMsgDto {
    @NotBlank(message = "regulation不能为空")
    private String regulation;
    private List<String> tasknames;
    private String taskstatus;
    private List<String> taskids;
}
