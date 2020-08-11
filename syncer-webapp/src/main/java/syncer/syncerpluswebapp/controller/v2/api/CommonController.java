package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.dao.DashBoardMapper;
import syncer.syncerpluswebapp.entity.SeriesDataEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/6
 */
@RestController
@RequestMapping("info")
public class CommonController {
    @Autowired
    private DashBoardMapper dashBoardMapper;

    @RequestMapping("/dashboardInfo")
    public ResultMap dashboard(){
        Map<String ,Integer>data=new HashMap<>();
        data.put("taskCount",dashBoardMapper.taskCount());
        data.put("brokenCount",dashBoardMapper.brokenCount());
        data.put("stopCount",dashBoardMapper.stopCount());
        data.put("runCount",dashBoardMapper.runCount());
        return ResultMap.builder().code("2000").data(data).msg("success");
    }

    @RequestMapping("/chardashboardInfo")
    public ResultMap chardashboard(){
        Map<String ,Object>data=new HashMap<>();
        String []legendData=new String[]{"实时同步", "RDB导入", "在线RDB导入", "AOF导入", "在线AOF导入","MIXED导入","在线MIXED导入"};
        data.put("legendData",legendData);
        data.put("seriesName","任务分类");

        List<SeriesDataEntity>list=new ArrayList<>();

        list.add(SeriesDataEntity.builder().name(legendData[0]).value(dashBoardMapper.syncCount()).build());

        list.add(SeriesDataEntity.builder().name(legendData[1]).value(dashBoardMapper.rdbCount()).build());

        list.add(SeriesDataEntity.builder().name(legendData[2]).value(dashBoardMapper.onlineRdbCount()).build());
        list.add(SeriesDataEntity.builder().name(legendData[3]).value(dashBoardMapper.aofCount()).build());
        list.add(SeriesDataEntity.builder().name(legendData[4]).value(dashBoardMapper.onlineAofCount()).build());
        list.add(SeriesDataEntity.builder().name(legendData[5]).value(dashBoardMapper.mixedCount()).build());
        list.add(SeriesDataEntity.builder().name(legendData[6]).value(dashBoardMapper.onlineMixedCount()).build());
        data.put("seriesData",list);
        return ResultMap.builder().code("2000").data(data).msg("success");
    }
}
