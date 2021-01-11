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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.common.bean.PageBean;
import syncer.common.entity.ResponseResult;
import syncer.transmission.model.RdbVersionModel;
import syncer.transmission.service.IRdbVersionService;
import syncer.webapp.constants.ApiConstants;
import syncer.webapp.request.DeleteRdbVersionParam;
import syncer.webapp.request.InsertRdbVersionParam;
import syncer.webapp.request.PageParam;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description RDB版本管理
 * @Date 2020/12/3
 */

@RestController
@RequestMapping(value = "/api/v2")
@Api( tags = "RDB版本映射管理接口")
@Validated
public class RdbController {
    @Autowired
    IRdbVersionService rdbVersionService;

    @ApiOperation(value="获取Redis版本与RDB映射版本列表", notes="获取Redis版本与RDB映射版本列表")
    @RequestMapping(value = "/getRdbList",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult<List<RdbVersionModel>> getRdbList()throws Exception{
        List<RdbVersionModel> rdbVersionModelList=rdbVersionService.findAllRdbVersion();
        return ResponseResult.<List<RdbVersionModel>>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(rdbVersionModelList)
                .build();
    }

    @ApiOperation(value="分页获取Redis版本与RDB映射版本列表", notes="分页获取Redis版本与RDB映射版本列表")
    @RequestMapping(value = "/getRdbListByPage",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult<PageBean<RdbVersionModel>> getRdbListByPage(@RequestBody PageParam pageParam)throws Exception{
        if (pageParam.getCurrentPage()==0){
            pageParam.setCurrentPage(1);
        }
        if (pageParam.getPageSize()==0){
            pageParam.setPageSize(10);
        }
        PageBean<RdbVersionModel> rdbVersionModelList=rdbVersionService.findRdbVersionModelByPage(pageParam.getCurrentPage(),pageParam.getPageSize());
        return ResponseResult.<PageBean<RdbVersionModel>>builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                .data(rdbVersionModelList)
                .build();
    }

    @ApiOperation(value="插入Redis版本与RDB映射版本信息", notes="新增Redis版本与RDB映射版本信息")
    @RequestMapping(value = "/insetRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult insertRdbVersion(@RequestBody @Validated InsertRdbVersionParam param)throws Exception{

        ResponseResult responseResult=null;
        boolean result=rdbVersionService.insertRdbVersion(RdbVersionModel.builder()
                .rdb_version(param.getRdb_version()).redis_version(param.getRedis_version()).build());

        if(result){
            responseResult=ResponseResult.builder()
                    .code(ApiConstants.SUCCESS_CODE)
                    .msg(ApiConstants.REQUEST_SUCCESS_MSG)
                    .build();
        }else {
            responseResult=ResponseResult.builder()
                    .code(ApiConstants.ERROR_CODE)
                    .msg(ApiConstants.INSERT_RDB_VERSION_ERROR_MSG)
                    .build();
        }

        return responseResult;
    }


    @ApiOperation(value="更新Redis版本与RDB映射版本信息", notes="修改Redis版本与RDB映射版本信息")
    @RequestMapping(value = "/updateRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult updateRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{

        rdbVersionService.updateRdbVersionModelById(rdbVersionModel.getId(),rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version());
        return ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.SUCCESS_MSG)
                .build();
    }

    
    @ApiOperation(value="删除Redis版本与RDB映射版本信息", notes="删除Redis版本与RDB映射版本信息")
    @RequestMapping(value = "/deleteRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResponseResult deleteRdbVersion(@RequestBody @Validated DeleteRdbVersionParam param)throws Exception{
        rdbVersionService.deleteRdbVersionModelById(param.getId());
        return ResponseResult.builder()
                .code(ApiConstants.SUCCESS_CODE)
                .msg(ApiConstants.SUCCESS_MSG)
                .build();
    }
}
