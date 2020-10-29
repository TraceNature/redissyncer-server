package syncer.syncerplusredis.entity.dto;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import syncer.syncerplusredis.entity.dto.common.SyncDataDto;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


/**
 * 新版dto
 */
@Getter
@Setter
@EqualsAndHashCode
public class RedisClusterDto extends SyncDataDto implements Serializable {
    private static final long serialVersionUID = -5809782578272943998L;
    @ApiModelProperty(value = "源redis地址",required = true, allowableValues = "cluster集群模式下地址由';'分割，如' 10.0.0.1:6379;10.0.0.2:6379'")
    @NotBlank(message = "源RedisCluster地址不能为空")
    private String sourceRedisAddress;
    @ApiModelProperty(value = "目标redis地址",required = true, allowableValues = "目标redis地址 ,当目标redis为单实例或proxy时，" +
            "填写单一地址即可，当目标redis为集群且需要借助jedis访问集群时地址用';'分割，'192.168.0.1:6379;192.168.0.3:6379;192.168.0.3:6379'")
    @NotBlank(message = "目标RedisCluster地址不能为空")
    private String targetRedisAddress;
    @ApiModelProperty(value = "源redis密码",required = true)
    private String sourcePassword;
    @ApiModelProperty(value = "目标redis密码",required = true)
    private String targetPassword;
    @NotBlank(message = "任务名称不能为空")
    @ApiModelProperty(value = "任务名")
    private String taskName;
    @ApiModelProperty(value = "是否自动启动", allowableValues = "默认值为false")
    @Builder.Default
    private boolean autostart=false;
    @ApiModelProperty(value = "任务是否从头开始", allowableValues = "如果之前进行过全量同步并且offset值还在积压缓冲区时，为false时则从offset+1值开始进行增量同步，" +
            "为true时则进行全量同步，缺省默认值为true (注：创建接口时 afresh字段仅和autostart为true时同时使用，afresh字段当startTask为必填字段)")
    @Builder.Default
    private boolean afresh=true;
    @ApiModelProperty(value = "pipline没次提交的最到key的数量", allowableValues = "默认值为500")
    private int batchSize;
    @ApiModelProperty(value = "任务类型", allowableValues = "默认值为500")
    @Builder.Default
    private String tasktype="total";
    @Builder.Default
    private String offsetPlace="endbuffer";

    public RedisClusterDto(@NotBlank(message = "源RedisCluster地址不能为空") String sourceRedisAddress, @NotBlank(message = "目标RedisCluster地址不能为空") String targetRedisAddress, String sourcePassword, String targetPassword, @NotBlank(message = "任务名称不能为空") String threadName, int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis, int diffVersion, String pipeline) {
        super(minPoolSize,maxPoolSize,maxWaitTime,timeBetweenEvictionRunsMillis,idleTimeRunsMillis,diffVersion,pipeline);
        this.sourceRedisAddress = sourceRedisAddress;
        this.targetRedisAddress = targetRedisAddress;
        this.sourcePassword = sourcePassword;
        this.targetPassword = targetPassword;
        this.taskName = threadName;

    }

    public RedisClusterDto() {
    }

    public RedisClusterDto(int minPoolSize, int maxPoolSize, long maxWaitTime, long timeBetweenEvictionRunsMillis, long idleTimeRunsMillis) {
        super(minPoolSize, maxPoolSize, maxWaitTime, timeBetweenEvictionRunsMillis, idleTimeRunsMillis);
    }

    public static void main(String[] args) {
        RedisClusterDto dto=new RedisClusterDto("","",
                "","","",10,1,1000
                ,10000,1000,9,"on");
        System.out.println(JSON.toJSONString(dto));
    }

}
