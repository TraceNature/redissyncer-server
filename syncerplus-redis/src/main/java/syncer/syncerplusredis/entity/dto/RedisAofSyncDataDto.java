package syncer.syncerplusredis.entity.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class RedisAofSyncDataDto {
    @NotBlank(message = "源redis AOf文件路径地址不能为空")
    private String sourceAofUri;
    @NotBlank(message = "目标redis路径不能为空")
    private String targetUri;
    @NotBlank(message = "任务名称不能为空")
    private String threadName;


    public RedisAofSyncDataDto(String sourceAofUri, String targetUri, String threadName) {
        this.sourceAofUri = sourceAofUri;
        this.targetUri = targetUri;
        this.threadName = threadName;
    }


}
