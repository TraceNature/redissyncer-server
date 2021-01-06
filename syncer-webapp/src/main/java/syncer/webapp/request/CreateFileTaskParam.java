package syncer.webapp.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.entity.FileType;

import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Objects;

/**
 * RDB AOF文件同步配置
 */
@Getter
@Setter
@EqualsAndHashCode
public class CreateFileTaskParam {
    private static final long serialVersionUID = -5809782578272943998L;
    private String taskId;


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
    private FileType fileType=FileType.RDB;


    @ApiModelProperty(value = "redis db映射关系", allowableValues = "当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库")
    private Map<Integer,Integer> dbMapper;


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

    public void setSynctype(FileType synctype) {
        if(Objects.nonNull(fileType)){
            this.fileType = synctype;
            this.synctype = synctype;
        }

    }

    public void setFileType(FileType fileType) {
        if(Objects.nonNull(fileType)){
            this.fileType = fileType;
            this.synctype = fileType;
        }

    }
}
