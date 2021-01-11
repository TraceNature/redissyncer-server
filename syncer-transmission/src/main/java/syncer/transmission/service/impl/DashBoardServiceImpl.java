// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.transmission.mapper.DashBoardMapper;
import syncer.transmission.po.CharDashBoardInfoDto;
import syncer.transmission.po.DashBoardDto;
import syncer.transmission.service.IDashService;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/9
 */
@Service
public class DashBoardServiceImpl implements IDashService {
    @Autowired
    private DashBoardMapper dashBoardMapper;
    @Override
    public DashBoardDto getDashboardInfo() {
        return DashBoardDto.builder()
                .brokenCount(dashBoardMapper.brokenCount())
                .runCount(dashBoardMapper.runCount())
                .stopCount(dashBoardMapper.stopCount())
                .taskCount(dashBoardMapper.taskCount())
                .build();
    }

    @Override
    public CharDashBoardInfoDto getCharDashBoardInfo() {

        return CharDashBoardInfoDto.builder()
                .syncCount(dashBoardMapper.syncCount())
                .aofCount(dashBoardMapper.aofCount())
                .mixedCount(dashBoardMapper.mixedCount())
                .onlineAofCount(dashBoardMapper.onlineAofCount())
                .onlineRdbCount(dashBoardMapper.onlineRdbCount())
                .onlineMixedCount(dashBoardMapper.onlineMixedCount())
                .rdbCount(dashBoardMapper.rdbCount())
                .build();
    }
}
