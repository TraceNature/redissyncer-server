package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.dao.DashBoardMapper;

import java.util.HashMap;
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
}
