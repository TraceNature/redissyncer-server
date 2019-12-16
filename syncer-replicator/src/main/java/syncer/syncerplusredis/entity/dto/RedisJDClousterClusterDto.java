package syncer.syncerplusredis.entity.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter

public class RedisJDClousterClusterDto {
    @NotBlank(message = "RedisCluster地址不能为空")
    private String jedisAddress;
    private String password;

    @NotBlank(message = "目标redis路径不能为空")
    private String targetUri;
    @NotBlank(message = "任务名称不能为空")
    private String threadName;
    private int minPoolSize;
    private int maxPoolSize;
    private long maxWaitTime;
    private long timeBetweenEvictionRunsMillis;
    private long idleTimeRunsMillis;


    public RedisJDClousterClusterDto(@NotBlank(message = "RedisCluster地址不能为空") String jedisAddress, String password) {
        this.jedisAddress = jedisAddress;
        this.password = password;

    }
}
