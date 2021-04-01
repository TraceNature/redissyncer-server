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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.common.entity.ResponseResult;
import syncer.common.montitor.Montitor;
import syncer.replica.type.FileType;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.service.ITaskGroupService;
import syncer.transmission.strategy.taskcheck.RedisTaskStrategyGroupType;
import syncer.transmission.strategy.taskcheck.TaskCheckStrategyGroupSelecter;
import syncer.webapp.config.submit.Resubmit;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.request.CreateDumpUpParam;
import syncer.webapp.request.CreateFileTaskParam;
import syncer.webapp.util.DtoToTaskModelUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/3
 */
@Api(tags = "文件任务创建接口")
@RestController
@RequestMapping(value = "/api/v2/file")
@Validated
@Slf4j
public class TaskGroupFileController {
    @Autowired
    Montitor montitor;
    @Autowired
    ITaskGroupService taskGroupService;

    @ApiOperation(value = "创建数据文件导入任务", notes = "支持AOF、RDB、混合文件 等本地/在线文件")
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




    /**
     * 备份实时命令
     * @param param
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "创建命令实时备份AOF任务", notes = "增量阶段生成AOF文件")
    @RequestMapping(value = "/createCommandDumpUpTask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @Resubmit(delaySeconds = 10)
    public ResponseResult<List<StartTaskEntity>> createCommandDumpUptask(@RequestBody @Validated CreateDumpUpParam param) throws Exception {
        param.setFileType(FileType.COMMANDDUMPUP);
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(param,false);

        /**
         * 检查策略
         */
        for (TaskModel taskModel : taskModelList) {
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.COMMANDUPGROUP, null, taskModel).run(null, taskModel);
        }
        List<StartTaskEntity>startTaskEntityList=taskGroupService.createCommandDumpUpTask(taskModelList);

        return ResponseResult.<List<StartTaskEntity>>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(startTaskEntityList)
                .build();
    }


}