package syncer.webapp.controller.v2.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.common.entity.ResponseResult;
import syncer.common.montitor.Montitor;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.service.ITaskGroupService;
import syncer.transmission.strategy.taskcheck.RedisTaskStrategyGroupType;
import syncer.transmission.strategy.taskcheck.TaskCheckStrategyGroupSelecter;
import syncer.webapp.config.submit.Resubmit;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.request.CreateFileTaskParam;
import syncer.webapp.util.DtoToTaskModelUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/3
 */
@RestController
@RequestMapping(value = "/api/v2/file")
@Validated
@Slf4j
public class TaskGroupFileController {
    @Autowired
    Montitor montitor;
    @Autowired
    ITaskGroupService taskGroupService;

    @RequestMapping(value = "/createtask", method = {RequestMethod.POST}, produces = "application/json;charset=utf-8;")
    @Resubmit(delaySeconds = 10)
    public ResponseResult<List<StartTaskEntity>> createtask(@RequestBody @Validated CreateFileTaskParam param) throws Exception {
        List<TaskModel> taskModelList = DtoToTaskModelUtils.getTaskModelList(param, false);
        if (Objects.isNull(taskModelList) || taskModelList.size() == 0) {
            return ResponseResult.<List<StartTaskEntity>>builder().code("1000").msg("任务列表为空，请检查填入任务信息(文件是否存在)").data(null).build();
        }
        /**
         * 检查策略
         */
        for (TaskModel taskModel : taskModelList) {
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.FILEGROUP, null, taskModel).run(null, taskModel);
        }

        //负载检测
        if (montitor.isAboveThreshold(Montitor.DEFAULT_THRESHOLD)) {
            return ResponseResult.<List<StartTaskEntity>>builder()
                    .code(ApiConstants.TASK_ABOVE_THRESHOLD_CODE)
                    .msg(ApiConstants.TASK_ABOVE_THRESHOLD_MSG)
                    .data(null)
                    .build();
        }
        List<StartTaskEntity> startTaskEntityList = taskGroupService.createRedisToRedisTask(taskModelList);
        return ResponseResult.<List<StartTaskEntity>>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(startTaskEntityList)
                .build();
    }



}