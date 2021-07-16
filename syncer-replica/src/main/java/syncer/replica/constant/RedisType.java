package syncer.replica.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**   taskmodel.sourceRedisType
 * 1 单机
 * 2 cluster
 * 3 file
 * 4 哨兵
 */

@AllArgsConstructor
@Getter
public enum RedisType {
    SINGLE(1,"单机模式"),
    CLUSTER(2,"cluster集群模式"),
    FILE(3,"文件模式"),
    SENTINEL(4,"哨兵模式"),
    KAFKA(5,"kafka"),
    NONE(-1,"失败");

    private Integer code;
    private String msg;

}
