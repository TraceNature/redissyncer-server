package syncer.syncerplusredis.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/25
 */
@Builder
@Getter @Setter
public class RedisStartCheckEntity {
    private RedisClusterDto clusterDto;
    private RedisFileDataDto redisFileDataDto;
    private FileCommandBackupDataDto fileCommandBackupDataDto;
    private RedisStartCheckTypeEnum startCheckType;
    /**
     * 多任务数据列表
     */
    private List<RedisClusterDto> redisClusterDtoList;


    /**
     * 多任务数据列表
     */
    private List<RedisFileDataDto> redisFileDataDtoList;

    /**
     * 命令实时备份列表
     */
    private List<FileCommandBackupDataDto> fileCommandBackupDataDtoList;


    private  String taskId;
    @Builder.Default
    private boolean afresh=true;
}
