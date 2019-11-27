package syncerservice.syncerplusredis.entity.dto.task;

import syncerservice.syncerplusredis.entity.FileType;
import syncerservice.syncerplusredis.entity.dto.common.SyncDataDto;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Map;


/**
 * RDB AOF文件同步配置
 */
@Getter
@Setter
@EqualsAndHashCode
public class EditRedisFileDataDto extends SyncDataDto {
    private static final long serialVersionUID = -5809782578272943998L;
    private String fileAddress;
    private String targetRedisAddress;
    private String targetPassword;
    private String taskName;
    @Builder.Default
    private boolean autostart=false;
    private int batchSize;
    @NotBlank(message = "任务id不能为空")
    private String taskId;
    //迁移类型：psync/文件
    @Builder.Default
    private FileType fileType=FileType.ONLINERDB;


    public EditRedisFileDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline, Map<Integer, Integer> dbNum) {
        super(minPoolSize, maxPoolSize, maxWaitTime, timeBetweenEvictionRunsMillis, idleTimeRunsMillis, diffVersion, pipeline, dbNum);
    }
    public void setRedisFileDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
       super.setMinPoolSize(minPoolSize);
        super.setMaxPoolSize(maxPoolSize);
        super.setMaxWaitTime(maxWaitTime);
        super.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        super.setIdleTimeRunsMillis(idleTimeRunsMillis);

    }

}
