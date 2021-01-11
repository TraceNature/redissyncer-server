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

package syncer.webapp.controller.v2.api;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.common.entity.ResponseResult;
import syncer.transmission.po.CharDashBoardInfoDto;
import syncer.transmission.po.DashBoardDto;
import syncer.transmission.service.IDashService;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.entity.SeriesData;
import syncer.webapp.response.SeriesDataResponse;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/3
 */
@RestController
@RequestMapping("info")
@Api( tags = "dashboard信息接口")
public class CommonController {
    @Autowired
    IDashService dashService;
    @ApiOperation(value="获取不同任务运行状态占比", notes="任务运行状态占比")
    @RequestMapping(value = "/dashboardInfo",method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseResult<DashBoardDto> dashboard(){
        return ResponseResult.<DashBoardDto>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.SUCCESS_MSG)
                .data(dashService.getDashboardInfo())
                .build();
    }
    @ApiOperation(value="获取不同任务类型占比", notes="任务类型占比")
    @RequestMapping(value = "/chardashboardInfo" ,method = {RequestMethod.GET,RequestMethod.POST})
    public ResponseResult<SeriesDataResponse> chardashboard(){

        String []legendData=new String[]{"实时同步", "RDB导入", "在线RDB导入", "AOF导入", "在线AOF导入","MIXED导入","在线MIXED导入"};
        List<SeriesData> list= Lists.newArrayList();
        CharDashBoardInfoDto charDashBoardInfo=dashService.getCharDashBoardInfo();
        list.add(SeriesData.builder().name(legendData[0]).value(charDashBoardInfo.getSyncCount()).build());
        list.add(SeriesData.builder().name(legendData[1]).value(charDashBoardInfo.getRdbCount()).build());
        list.add(SeriesData.builder().name(legendData[2]).value(charDashBoardInfo.getOnlineRdbCount()).build());
        list.add(SeriesData.builder().name(legendData[3]).value(charDashBoardInfo.getAofCount()).build());
        list.add(SeriesData.builder().name(legendData[4]).value(charDashBoardInfo.getOnlineAofCount()).build());
        list.add(SeriesData.builder().name(legendData[5]).value(charDashBoardInfo.getMixedCount()).build());
        list.add(SeriesData.builder().name(legendData[6]).value(charDashBoardInfo.getOnlineMixedCount()).build());
        SeriesDataResponse response=SeriesDataResponse.builder()
                .seriesName("任务分类")
                .legendData(legendData)
                .seriesData(list)
                .build();

        return ResponseResult.<SeriesDataResponse>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .data(response)
                .msg(ApiConstants.SUCCESS_MSG)
                .build();
    }
}
