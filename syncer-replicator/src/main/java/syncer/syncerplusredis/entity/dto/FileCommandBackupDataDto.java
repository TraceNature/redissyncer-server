package syncer.syncerplusredis.entity.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.entity.FileType;
import syncer.syncerplusredis.entity.dto.common.SyncDataDto;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 增量数据备份
 * @Date 2020/1/2
 */
@Getter
@Setter
@EqualsAndHashCode
public class FileCommandBackupDataDto extends SyncDataDto {
    private static final long serialVersionUID = -5809782578272943998L;
    @NotBlank(message = "AOF存放地址不能为空")
    private String fileAddress;
    @NotBlank(message = "源Redis地址不能为空")
    private String sourceRedisAddress;
    private String sourcePassword;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    @Builder.Default
    private boolean autostart=false;



    public FileCommandBackupDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline, Map<Integer, Integer> dbNum) {
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
