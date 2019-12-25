package syncer.syncerplusredis.entity.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description 数据校验相关
 * @Date 2019/12/20
 */
@Getter
@Setter
@EqualsAndHashCode

public class RedisSyncNumCheckDto {
    @NotBlank(message = "源Redis地址不能为空")
    private String sourceRedisAddress;
    @NotBlank(message = "目标Redis地址不能为空")
    private String targetRedisAddress;
    private String sourcePassword;
    private String targetPassword;
    private Set<String>sourceRedisAddressSet;
    private Set<String>targetRedisAddressSet;

    public Set<String> getSourceRedisAddressSet() {
        if(null==sourceRedisAddressSet||sourceRedisAddressSet.size()==0){
            sourceRedisAddressSet=new HashSet<>(Arrays.asList(sourceRedisAddress.split(";")));
        }
        return sourceRedisAddressSet;
    }

    public Set<String> getTargetRedisAddressSet() {
        if(null==targetRedisAddressSet||targetRedisAddressSet.size()==0){
            targetRedisAddressSet=new HashSet<>(Arrays.asList(targetRedisAddress.split(";")));
        }
        return targetRedisAddressSet;
    }
}
