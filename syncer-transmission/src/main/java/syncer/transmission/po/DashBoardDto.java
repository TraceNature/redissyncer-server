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
public class DashBoardDto {
    private Integer taskCount;
    private Integer brokenCount;
    private Integer stopCount;
    private Integer runCount;
}
