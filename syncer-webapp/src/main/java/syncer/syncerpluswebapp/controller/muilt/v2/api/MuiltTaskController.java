package syncer.syncerpluswebapp.controller.muilt.v2.api;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.StartTaskEntity;
import syncer.syncerplusredis.entity.dto.MuliListParams;
import syncer.syncerplusredis.entity.dto.task.ListTaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;
import syncer.syncerplusredis.entity.muli.multisync.ParentMultiTaskModel;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltCreateTaskData;
import syncer.syncerplusredis.entity.muli.multisync.dto.MuiltDtoToModelUtils;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerpluswebapp.config.submit.Resubmit;

import syncer.syncerservice.filter.mulitSync.IMulitCommonStrategy;
import syncer.syncerservice.filter.mulitSync.MulitStrategySelecter;
import syncer.syncerservice.filter.mulitSync.MulitSyncStrategyGroupType;
import syncer.syncerservice.service.muilt.MuiltTaskService;
import syncer.syncerservice.util.common.Montitor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 双向任务管理
 * @Date 2020/10/12
 */
@RestController
@RequestMapping("/multi")
public class MuiltTaskController {
    @Autowired
    MuiltTaskService muiltTaskService;
    @Autowired
    Montitor montitor;


    @RequestMapping(value = "/createtask")
    @Resubmit(delaySeconds = 10)
    public ResultMap createTask(@RequestBody @Validated MuiltCreateTaskData data) throws Exception {
        ParentMultiTaskModel parentMultiTaskModel=MuiltDtoToModelUtils.getParentMuiltiTaskModel(data);
        IMulitCommonStrategy strategyList= MulitStrategySelecter.select(MulitSyncStrategyGroupType.NODISTINCT,data,parentMultiTaskModel);
        strategyList.run(data,parentMultiTaskModel);
        double montitors = new BigDecimal((float)montitor.jvmMemoryUsed()/montitor.jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        if(montitors>=0.80){
            return ResultMap.builder().code("1005").msg("当前系统已处于高负载状态,已开启任务数量限制，请稍后再创建任务");
        }

        return muiltTaskService.createMuilTask(parentMultiTaskModel);
    }



    /**
     * 根据taskId停止任务
     * @param params
     * @return
     */
    @RequestMapping(value = "/stoptask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")

    @Resubmit(delaySeconds = 10)
    public ResultMap stopTask( @RequestBody @Validated TaskMsgDto params) throws Exception {
        List<StartTaskEntity> msg=null;
        if(params.getTaskids()==null){
            return  ResultMap.builder().code("4000").msg("taskids不能为空");
        }
        return  muiltTaskService.stopMuilTask(params.getTaskids());
    }


    /**
     * 根据taskId查询任务列表
     * @param params
     * @return
     */
    @RequestMapping(value = "/listtasksByPage",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")

    public ResultMap listTaskByPage(@ApiIgnore @RequestBody @Validated MuliListParams params) throws Exception {
//        PageBean<TaskModelResult> listCreateThread= TaskDataManagerUtils.listTaskListByPages(listTaskMsgDto);
        return  ResultMap.builder().code("2000").msg("The request is successful").data(MultiSyncTaskManagerutils.listTaskList(params.getCurrentPage(),params.getPageSize()));
    }



    /**
     * 根据taskId启动任务
     * @param params
     * @return
     */
    @RequestMapping(value = "/starttask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")

    @Resubmit(delaySeconds = 10)
    public ResultMap startTask( @RequestBody @Validated TaskStartMsgDto params) throws Exception {
        if(StringUtils.isEmpty(params.getTaskid())){
            return  ResultMap.builder().code("4000").msg("taskid不能为空");
        }

//        double montitors = new BigDecimal((float)montitor.jvmMemoryUsed()/montitor.jvmMemoryMax()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//        if(montitors>=0.80){
//            return ResultMap.builder().code("1005").msg("当前系统已处于高负载状态,已开启任务数量限制，请稍后再创建任务");
//        }


        ResultMap resultMap=muiltTaskService.startTaskByTaskId(params.getTaskid(),false);
        return  resultMap;

    }
}
