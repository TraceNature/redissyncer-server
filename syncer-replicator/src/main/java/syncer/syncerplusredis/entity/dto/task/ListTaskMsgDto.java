package syncer.syncerplusredis.entity.dto.task;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListTaskMsgDto {
    @NotBlank(message = "regulation不能为空")
    private String regulation;
    @Builder.Default
    private List<String> tasknames=new ArrayList<>();
    private String taskstatus;
    @Builder.Default
    private List<String> taskids=new ArrayList<>();
    @Builder.Default
    private List<String> groupIds=new ArrayList<>();
}
