package syncer.syncerplusredis.entity.muli.multisync.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/10/12
 */

@Getter
@Setter
@EqualsAndHashCode

public class MuiltCreateTaskData {
    @NotBlank(message = "源Redis地址不能为空")
    private String sourceRedisAddress;
    @NotBlank(message = "目标Redis地址不能为空")
    private String targetRedisAddress;
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    @NotBlank(message = "源redis版本不能为空")
    //源redis节点版本
    private String sourceVersion;
    @NotBlank(message = "目标redis版本不能为空")
    //目标redis节点版本
    private String targetVersion;
    @Builder.Default
    private boolean acl=false;


    @Builder.Default
    private boolean autostart=false;

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MuiltCreateTaskData that = (MuiltCreateTaskData) o;

        if (acl != that.acl) {
            return false;
        }
        if (sourceRedisAddress != null ? !sourceRedisAddress.equals(that.sourceRedisAddress) : that.sourceRedisAddress != null)
        {
            return false;
        }
        if (targetRedisAddress != null ? !targetRedisAddress.equals(that.targetRedisAddress) : that.targetRedisAddress != null)
        {
            return false;
        }
        if (taskName != null ? !taskName.equals(that.taskName) : that.taskName != null){
            return false;
        }
        if (sourceVersion != null ? !sourceVersion.equals(that.sourceVersion) : that.sourceVersion != null)
        {
            return false;
        }
        return targetVersion != null ? targetVersion.equals(that.targetVersion) : that.targetVersion == null;
    }

    @Override
    public int hashCode() {
        int result = sourceRedisAddress != null ? sourceRedisAddress.hashCode() : 0;
        result = 31 * result + (targetRedisAddress != null ? targetRedisAddress.hashCode() : 0);
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        result = 31 * result + (sourceVersion != null ? sourceVersion.hashCode() : 0);
        result = 31 * result + (targetVersion != null ? targetVersion.hashCode() : 0);
        return result;
    }
}
