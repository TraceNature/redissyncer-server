package syncer.syncerservice.po;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/1/16
 */

@Setter@Getter
@Builder
public class KeyValueSizeEntity {
    private long size;

    public KeyValueSizeEntity(long size) {
        this.size = size;
    }
}
