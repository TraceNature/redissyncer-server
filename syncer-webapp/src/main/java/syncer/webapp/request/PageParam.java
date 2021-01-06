package syncer.webapp.request;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 分页参数
 * @Date 2020/12/7
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter

public class PageParam {
    @Builder.Default
    int currentPage=1;
    @Builder.Default
    int pageSize=10;
}
