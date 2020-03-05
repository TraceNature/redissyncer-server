package syncer.syncerplusredis.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhanenqiang
 * @Description 通过获取redis版本策略时，是否获取版本号
 * @Date 2020/2/27
 */
@Builder
@Getter@Setter
public class RedisVersionGetterEntity {
    @Builder.Default
    private boolean getTargetVersion=false;
    @Builder.Default
    private boolean getSourceVersion=false;
}
