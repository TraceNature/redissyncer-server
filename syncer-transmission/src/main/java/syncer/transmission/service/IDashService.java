package syncer.transmission.service;

import syncer.transmission.po.CharDashBoardInfoDto;
import syncer.transmission.po.DashBoardDto;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/9
 */
public interface IDashService {
    DashBoardDto getDashboardInfo();
    CharDashBoardInfoDto getCharDashBoardInfo();
}
