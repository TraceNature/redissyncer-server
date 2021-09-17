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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import syncer.replica.constant.RedisType;
import syncer.replica.type.FileType;
import syncer.transmission.constants.CommandKeyFilterType;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/10
 */
@Data
public class CreateTaskParam extends BaseTaskParam implements Serializable {

    public CreateTaskParam(@NotBlank(message = "源RedisCluster地址不能为空") String sourceRedisAddress, String targetRedisAddress, String sourcePassword, String targetPassword, @NotBlank(message = "任务名称不能为空") String taskName, boolean autostart, boolean afresh, String taskId, int batchSize, boolean circleReplication, String targetNodeId, boolean sourceAcl, boolean targetAcl, String sourceUserName, String sourceSentinelAuthPassword, RedisType sourceRedisType, String targetUserName, String sourceRedisMasterName, String targetRedisMasterName, String targetSentinelAuthPassword, RedisType targetRedisType, @NotBlank(message = "目标Redis版本不能为空") String targetRedisVersion, long errorCount, FileType synctype, FileType fileType, String tasktype, String offsetPlace, Map<Integer, Integer> dbMapper, Long timeDeviation, String commandFilter, String keyFilter, CommandKeyFilterType filterType, String topicName, String targetKafkaAddress, boolean rewrite) {
        super(sourceRedisAddress, targetRedisAddress, sourcePassword, targetPassword, taskName, autostart, afresh);
        this.taskId = taskId;
        this.batchSize = batchSize;
        this.circleReplication = circleReplication;
        this.targetNodeId = targetNodeId;
        this.sourceAcl = sourceAcl;
        this.targetAcl = targetAcl;
        this.sourceUserName = sourceUserName;
        this.sourceSentinelAuthPassword = sourceSentinelAuthPassword;
        this.sourceRedisType = sourceRedisType;
        this.targetUserName = targetUserName;
        this.sourceRedisMasterName = sourceRedisMasterName;
        this.targetRedisMasterName = targetRedisMasterName;
        this.targetSentinelAuthPassword = targetSentinelAuthPassword;
        this.targetRedisType = targetRedisType;
        this.targetRedisVersion = targetRedisVersion;
        this.errorCount = errorCount;
        this.synctype = synctype;
        this.fileType = fileType;
        this.tasktype = tasktype;
        this.offsetPlace = offsetPlace;
        this.dbMapper = dbMapper;
        this.timeDeviation = timeDeviation;
        this.commandFilter = commandFilter;
        this.keyFilter = keyFilter;
        this.filterType = filterType;
        this.topicName = topicName;
        this.targetKafkaAddress = targetKafkaAddress;
        this.rewrite = rewrite;
    }

    public CreateTaskParam(String taskId, int batchSize, boolean circleReplication, String targetNodeId, boolean sourceAcl, boolean targetAcl, String sourceUserName, String sourceSentinelAuthPassword, RedisType sourceRedisType, String targetUserName, String sourceRedisMasterName, String targetRedisMasterName, String targetSentinelAuthPassword, RedisType targetRedisType, @NotBlank(message = "目标Redis版本不能为空") String targetRedisVersion, long errorCount, FileType synctype, FileType fileType, String tasktype, String offsetPlace, Map<Integer, Integer> dbMapper, Long timeDeviation, String commandFilter, String keyFilter, CommandKeyFilterType filterType, String topicName, String targetKafkaAddress, boolean rewrite) {
        this.taskId = taskId;
        this.batchSize = batchSize;
        this.circleReplication = circleReplication;
        this.targetNodeId = targetNodeId;
        this.sourceAcl = sourceAcl;
        this.targetAcl = targetAcl;
        this.sourceUserName = sourceUserName;
        this.sourceSentinelAuthPassword = sourceSentinelAuthPassword;
        this.sourceRedisType = sourceRedisType;
        this.targetUserName = targetUserName;
        this.sourceRedisMasterName = sourceRedisMasterName;
        this.targetRedisMasterName = targetRedisMasterName;
        this.targetSentinelAuthPassword = targetSentinelAuthPassword;
        this.targetRedisType = targetRedisType;
        this.targetRedisVersion = targetRedisVersion;
        this.errorCount = errorCount;
        this.synctype = synctype;
        this.fileType = fileType;
        this.tasktype = tasktype;
        this.offsetPlace = offsetPlace;
        this.dbMapper = dbMapper;
        this.timeDeviation = timeDeviation;
        this.commandFilter = commandFilter;
        this.keyFilter = keyFilter;
        this.filterType = filterType;
        this.topicName = topicName;
        this.targetKafkaAddress = targetKafkaAddress;
        this.rewrite = rewrite;
    }

    public CreateTaskParam() {
    }

    private static final long serialVersionUID = -5809782578272943998L;
    /**
     * 任务id
     */
    private String taskId;
    @ApiModelProperty(value = "pipline没次提交的最到key的数量", allowableValues = "默认值为500")
    private int batchSize;

    private boolean circleReplication;

    private String targetNodeId;

    @Builder.Default
    private boolean sourceAcl=false;

    @Builder.Default
    private boolean targetAcl=false;

    /**
     * 源用户名
     */
    @Builder.Default
    private String sourceUserName="";

    /**
     * 源哨兵密码
     */
    @Builder.Default
    private String sourceSentinelAuthPassword="";

    /**
     * 源Redis类型
     *
     */
    private RedisType sourceRedisType;

    /**
     * 目标用户名
     */
    @Builder.Default
    private String targetUserName="";

    /**
     * sentinel masterName
     */
    @Builder.Default
    private String sourceRedisMasterName="";

    /**
     * sentinel masterName
     */
    @Builder.Default
    private String targetRedisMasterName="";


    /**
     * 目标哨兵密码
     */
    @Builder.Default
    private String targetSentinelAuthPassword="";

    /**
     * 目标Redis类型
     */
    private RedisType targetRedisType;

    /**
     * 目标Redis版本
     */
    @NotBlank(message = "目标Redis版本不能为空")
    private String targetRedisVersion;

    /**
     * 抛弃Key阈值
     */
    @Builder.Default
    private long errorCount=1;

    /**
     * 迁移类型：psync/文件
     */
    @ApiModelProperty(value = "迁移类型", allowableValues = "SYNC")
    @Builder.Default
    private FileType synctype;

    /**
     * 同 synctype 旧版本为fileType
     */
    @Builder.Default
    private FileType fileType;

    @Builder.Default
    private String tasktype="total";

    @Builder.Default
    private String offsetPlace="endbuffer";


    @ApiModelProperty(value = "redis db映射关系", allowableValues = "当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库")
    private Map<Integer,Integer> dbMapper;

    /**
     * 时间偏差
     */
    @Builder.Default
    private Long timeDeviation=0L;

    /**
     * 命令过滤器
     */
    @Builder.Default
    private String commandFilter="";

    /**
     * Key过滤器
     */
    @Builder.Default
    private String keyFilter="";


    @Builder.Default
    private CommandKeyFilterType filterType=CommandKeyFilterType.NONE;

    @Builder.Default
    private String topicName="";

    @Builder.Default
    private String targetKafkaAddress="";

    /**
     * 目标存在相同key时是否覆盖
     */
    @Builder.Default
    private boolean rewrite=false;

    public Map<Integer, Integer> getDbMapper() {
        if(dbMapper!=null){
            return dbMapper;
        }
        return new HashMap<Integer,Integer>();
    }

}
