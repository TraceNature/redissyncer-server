package syncer.webapp.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/10
 */
@Data
@Builder
public class SeriesDataResponse {
    private String[]legendData;
    private String seriesName;
    private List seriesData;


}
