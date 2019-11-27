package syncerservice.syncerplusredis.entity.dto.task;

import syncerservice.syncerplusredis.entity.dto.common.SyncDataDto;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
@Getter
@Setter
@EqualsAndHashCode
public class EditRedisClusterDto extends SyncDataDto implements Serializable {
    private String sourceRedisAddress;
    private String targetRedisAddress;
    private String sourcePassword;
    private String targetPassword;
    private String taskName;
    @NotBlank(message = "任务id不能为空")
    private String taskId;
    @Builder.Default
    private boolean autostart=false;
    @Builder.Default
    private boolean afresh=true;

    private int batchSize;
    public EditRedisClusterDto(String sourceRedisAddress,  String targetRedisAddress, String sourcePassword, String targetPassword,  String threadName, int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline) {
        super(minPoolSize,maxPoolSize,maxWaitTime,timeBetweenEvictionRunsMillis,idleTimeRunsMillis,diffVersion,pipeline);
        this.sourceRedisAddress = sourceRedisAddress;
        this.targetRedisAddress = targetRedisAddress;
        this.sourcePassword = sourcePassword;
        this.targetPassword = targetPassword;
        this.taskName = threadName;

    }
}
