package syncer.common.bean;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/22
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PageParamBean {
    @Builder.Default
    int currentPage=1;
    @Builder.Default
    int pageSize=10;

}
