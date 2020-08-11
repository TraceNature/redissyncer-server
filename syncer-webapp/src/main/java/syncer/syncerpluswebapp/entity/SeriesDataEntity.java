package syncer.syncerpluswebapp.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/11
 */
@Data
@Builder

public class SeriesDataEntity {
    private int value;
    private String name;
}
