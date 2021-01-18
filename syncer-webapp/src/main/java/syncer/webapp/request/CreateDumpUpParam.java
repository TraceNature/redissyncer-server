package syncer.webapp.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.entity.FileType;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 创建aof实施备份文件任务参数
 */
@Getter
@Setter
@EqualsAndHashCode

public class CreateDumpUpParam {
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
    //迁移类型：psync/文件
    @Builder.Default
    private FileType fileType= FileType.SYNC;
    @ApiModelProperty(value = "redis db映射关系", allowableValues = "当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库")
    private Map<Integer,Integer>dbMapper;

    private String taskId;
    @Builder.Default
    private boolean sourceAcl=false;

    @Builder.Default
    private boolean targetAcl=false;

    //源用户名
    @Builder.Default
    private String sourceUserName="";
    //目标用户名
    @Builder.Default
    private String targetUserName="";
    /**
     * 抛弃Key阈值
     */
    @Builder.Default
    private long errorCount=1;

    public Map<Integer, Integer> getDbMapper() {
        if(Objects.nonNull(dbMapper)){
            return dbMapper;
        }
        return new HashMap<Integer,Integer>();
    }

}
