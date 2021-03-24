// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.webapp.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/10
 */
@Getter
@Setter
@EqualsAndHashCode
public class BaseTaskParam implements Serializable {
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

    public BaseTaskParam() {
    }

    public BaseTaskParam(@NotBlank(message = "源RedisCluster地址不能为空") String sourceRedisAddress, @NotBlank(message = "目标RedisCluster地址不能为空") String targetRedisAddress, String sourcePassword, String targetPassword, @NotBlank(message = "任务名称不能为空") String taskName, boolean autostart, boolean afresh) {
        this.sourceRedisAddress = sourceRedisAddress;
        this.targetRedisAddress = targetRedisAddress;
        this.sourcePassword = sourcePassword;
        this.targetPassword = targetPassword;
        this.taskName = taskName;
        this.autostart = autostart;
        this.afresh = afresh;
    }
}
