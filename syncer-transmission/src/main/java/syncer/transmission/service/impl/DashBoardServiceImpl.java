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

import org.springframework.stereotype.Service;
import syncer.transmission.po.CharDashBoardInfoDto;
import syncer.transmission.po.DashBoardDto;
import syncer.transmission.service.IDashService;
import syncer.transmission.util.sql.SqlOPUtils;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/9
 */
@Service
public class DashBoardServiceImpl implements IDashService {


    @Override
    public DashBoardDto getDashboardInfo() {
        return DashBoardDto.builder()
                .brokenCount(SqlOPUtils.brokenCount())
                .runCount(SqlOPUtils.runCount())
                .stopCount(SqlOPUtils.stopCount())
                .taskCount(SqlOPUtils.taskCount())
                .build();
    }

    @Override
    public CharDashBoardInfoDto getCharDashBoardInfo() {

        return CharDashBoardInfoDto.builder()
                .syncCount(SqlOPUtils.syncCount())
                .aofCount(SqlOPUtils.aofCount())
                .mixedCount(SqlOPUtils.mixedCount())
                .onlineAofCount(SqlOPUtils.onlineAofCount())
                .onlineRdbCount(SqlOPUtils.onlineRdbCount())
                .onlineMixedCount(SqlOPUtils.onlineMixedCount())
                .rdbCount(SqlOPUtils.rdbCount())
                .build();
    }
}
