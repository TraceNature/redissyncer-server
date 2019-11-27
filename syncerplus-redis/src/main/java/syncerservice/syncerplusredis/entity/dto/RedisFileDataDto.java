package syncerservice.syncerplusredis.entity.dto;

import syncerservice.syncerplusredis.entity.FileType;
import syncerservice.syncerplusredis.entity.dto.common.SyncDataDto;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Map;


/**
 * RDB AOF文件同步配置
 */
@Getter
@Setter
@EqualsAndHashCode
public class RedisFileDataDto extends SyncDataDto {
    private static final long serialVersionUID = -5809782578272943998L;
    @NotBlank(message = "AOF/RDB 地址不能为空")
    private String fileAddress;
    @NotBlank(message = "目标RedisCluster地址不能为空")
    private String targetRedisAddress;
    private String targetPassword;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    @Builder.Default
    private boolean autostart=false;
    private int batchSize;


    //迁移类型：psync/文件
    @Builder.Default
    private FileType fileType=FileType.ONLINERDB;


    public RedisFileDataDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline, Map<Integer, Integer> dbNum) {
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
