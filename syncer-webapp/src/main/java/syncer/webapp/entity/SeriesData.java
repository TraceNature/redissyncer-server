package syncer.webapp.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/10
 */
@Data
@Builder
public class SeriesData {
    private int value;
    private String name;
}
