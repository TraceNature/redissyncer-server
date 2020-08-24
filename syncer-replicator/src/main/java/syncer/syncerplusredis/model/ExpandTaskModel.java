package syncer.syncerplusredis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description Task扩展类对应 expandJson 字段
 * @Date 2020/8/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpandTaskModel {
    @Builder.Default
    private String brokenResult="";



}
