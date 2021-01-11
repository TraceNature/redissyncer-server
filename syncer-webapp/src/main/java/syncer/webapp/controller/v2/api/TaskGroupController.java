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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.common.bean.PageBean;
import syncer.common.entity.ResponseResult;
import syncer.common.exception.TaskMsgException;
import syncer.common.montitor.Montitor;
import syncer.transmission.constants.TaskMsgConstant;
import syncer.transmission.entity.StartTaskEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.po.ListTaskParamDto;
import syncer.transmission.po.TaskModelResult;
import syncer.transmission.service.ITaskGroupService;
import syncer.transmission.strategy.taskcheck.RedisTaskStrategyGroupType;
import syncer.transmission.strategy.taskcheck.TaskCheckStrategyGroupSelecter;
import syncer.transmission.util.code.CodeUtils;
import syncer.webapp.config.submit.Resubmit;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.request.*;
import syncer.webapp.util.DtoToTaskModelUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/3
 */

@Api("/api/v2")
@RestController
@RequestMapping(value = "/api/v2")
@Validated
@Slf4j
public class TaskGroupController {
    @Autowired
    Montitor montitor;
    @Autowired
    ITaskGroupService taskGroupService;

    /**
     * 创建同步任务
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "创建实时同步任务接口", notes = "用于在线(SYNC)任务创建")
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @Resubmit(delaySeconds = 10)
    public ResponseResult createTask(@RequestBody @Validated CreateTaskParam params) throws Exception {
        List<TaskModel> taskModelList= DtoToTaskModelUtils.getTaskModelList(params,false);
        if(Objects.isNull(taskModelList)||taskModelList.size()==0){
            return ResponseResult.builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.TASK_CREATE_LIST_EMPTY)
                    .build();
        }

        for (TaskModel taskModel : taskModelList) {
            TaskCheckStrategyGroupSelecter.select(RedisTaskStrategyGroupType.SYNCGROUP,null,taskModel).run(null,taskModel);
        }
        //负载检测
        if(montitor.isAboveThreshold(Montitor.DEFAULT_THRESHOLD)){
            return ResponseResult.builder()
                    .code(ApiConstants.TASK_ABOVE_THRESHOLD_CODE)
                    .msg(ApiConstants.TASK_ABOVE_THRESHOLD_MSG)
                    .build();
        }
        List<StartTaskEntity> startTaskEntityList=taskGroupService.createRedisToRedisTask(taskModelList);
        return ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(startTaskEntityList)
                .build();
    }


    /**
     * 停止任务
     * @param params
     * @return
     * @throws Exception
     */

    @ApiOperation(value ="停止任务")
    @RequestMapping(value = "/stoptask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @Resubmit(delaySeconds = 10)
    public ResponseResult stopTask( @RequestBody @Validated StopTaskParam params) throws Exception {
        List<StartTaskEntity> msg=null;
        if(Objects.isNull(params.getTaskids())&&Objects.isNull(params.getGroupIds())){
            return  ResponseResult.builder()
                    .code(ApiConstants.TASK_TASK_GROUP_ID_NOT_NULL_CODE)
                    .msg(ApiConstants.TASK_TASK_GROUP_ID_NOT_NULL_MSG)
                    .build();
        }
        if(Objects.nonNull(params.getGroupIds())&&params.getGroupIds().size()>0){
            log.info("手动触发停止任务[groupId]...{}",params.getGroupIds());
            msg= taskGroupService.batchStopTaskListByGroupIdList(params.getGroupIds());
        }else {
            log.info("手动触发停止任务[taskId]...{}",params.getTaskids());
            msg= taskGroupService.batchStopTaskListByTaskIdList(params.getTaskids());
        }
        return  ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(msg)
                .build();
    }

    /**
     * 根据taskId启动任务
     * @param param
     * @return
     */
    @RequestMapping(value = "/starttask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @ApiOperation(value ="启动任务")
    @Resubmit(delaySeconds = 10)
    public ResponseResult startTask( @RequestBody @Validated  StartTaskParam param) throws Exception {
        if(StringUtils.isEmpty(param.getTaskid())&&StringUtils.isEmpty(param.getGroupId())){
            return  ResponseResult
                    .builder()
                    .code(ApiConstants.TASK_TASK_GROUP_ID_NOT_NULL_CODE)
                    .msg(ApiConstants.TASK_TASK_GROUP_ID_NOT_NULL_MSG)
                    .build();
        }

        //负载检测
        if (montitor.isAboveThreshold(Montitor.DEFAULT_THRESHOLD)) {
            return ResponseResult.<List<StartTaskEntity>>builder()
                    .code(ApiConstants.TASK_ABOVE_THRESHOLD_CODE)
                    .msg(ApiConstants.TASK_ABOVE_THRESHOLD_MSG)
                    .data(null)
                    .build();
        }


        if(!StringUtils.isEmpty(param.getTaskid())){
            List<StartTaskEntity>startTaskEntityList= Lists.newArrayList();
            startTaskEntityList.add(taskGroupService.startTaskByTaskId(param.getTaskid(),param.isAfresh()));
            ResponseResult result=ResponseResult.builder()
                    .code(ApiConstants.SUCCESS_CODE)
                    .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                    .data(startTaskEntityList)
                    .build();
            return  result;
        }else if(!StringUtils.isEmpty(param.getGroupId())){
            ResponseResult result=ResponseResult.builder()
                    .code(ApiConstants.SUCCESS_CODE)
                    .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                    .data(taskGroupService.batchStartTaskListByGroupId(param.getGroupId(),param.isAfresh()))
                    .build();
            return  result;
        }
        return  ResponseResult.builder()
                .code(ApiConstants.TASK_TASK_GROUP_ID_NOT_NULL_CODE)
                .msg(ApiConstants.TASK_PARAM_ERROR)
                .build();
    }


    /**
     * 根据taskId查询任务列表
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/listtasksByPage",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult listTaskByPage(@RequestBody @Validated ListTaskParam param) throws Exception {
        checkListTaskParam(param);
        ListTaskParamDto listTaskParamDto= ListTaskParamDto.builder().build();
        BeanUtils.copyProperties(param,listTaskParamDto);
        PageBean<TaskModelResult> listTask=taskGroupService.listTaskListByPages(listTaskParamDto);
        return  ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(listTask)
                .build();
    }

    /**
     * 根据taskId查询任务列表
     * @param param
     * @return
     * @throws Exception
     */

    @RequestMapping(value = "/listtasks",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @ApiOperation(value="任务查询")
    public ResponseResult listTask(@RequestBody @Validated ListTaskParam param) throws Exception {
        checkListTaskParam(param);
        ListTaskParamDto listTaskParamDto= ListTaskParamDto.builder().build();
        BeanUtils.copyProperties(param,listTaskParamDto);
        List<TaskModelResult>  listTask=taskGroupService.listTaskList(listTaskParamDto);
        return  ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(listTask)
                .build();
    }

    void checkListTaskParam(ListTaskParam param) throws TaskMsgException {
        if(param.getCurrentPage()==0){
            param.setCurrentPage(1);
        }
        if(param.getPageSize()==0){
            param.setPageSize(10);
        }
        //all、bynames、byids、bystatus
        if(!"all".equals(param.getRegulation().trim())
                &&!"bynames".equals(param.getRegulation().trim())
                && !"byids".equals(param.getRegulation().trim())
                && !"bystatus".equals(param.getRegulation().trim())
                && !"byGroupIds".equals(param.getRegulation().trim())
        ){
            throw new TaskMsgException(CodeUtils.codeMessages(TaskMsgConstant.TASK_MSG_TASKID_REGULATION_ERROR_CODE,TaskMsgConstant.TASK_MSG_TASKID_REGULATION_ERROR));
        }
    }

    @RequestMapping(value = "/removetask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    @ApiOperation("删除任务")
    @Resubmit(delaySeconds = 10)
    public ResponseResult deleteTask(@RequestBody @Validated RemoveTaskParam params) throws Exception {
        List<StartTaskEntity> msg=null;
        if(Objects.isNull(params.getTaskids())&&Objects.isNull(params.getGroupIds())){
            return  ResponseResult.builder().code("4000").msg("taskids或GroupId不能为空").build();
        }
        if(Objects.nonNull(params.getGroupIds())&&params.getGroupIds().size()>0){
            msg= taskGroupService.removeTaskByGroupIdList(params.getGroupIds());
        }else {
            msg= taskGroupService.removeTaskByTaskIdList(params.getTaskids());
        }
        return ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(msg)
                .build();
    }
}
