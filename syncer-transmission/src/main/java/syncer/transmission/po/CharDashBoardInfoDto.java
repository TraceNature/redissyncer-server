package syncer.transmission.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CharDashBoardInfoDto {
    private Integer syncCount;
    private Integer rdbCount;
    private Integer onlineRdbCount;
    private Integer aofCount;
    private Integer onlineAofCount;
    private Integer mixedCount;
    private Integer onlineMixedCount;
}
