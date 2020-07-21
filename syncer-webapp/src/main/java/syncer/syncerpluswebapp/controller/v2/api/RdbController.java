package syncer.syncerpluswebapp.controller.v2.api;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerpluscommon.bean.PageBean;
import syncer.syncerpluscommon.bean.PageParamBean;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.dao.RdbVersionMapper;
import syncer.syncerplusredis.model.RdbVersionModel;

import syncer.syncerservice.service.IRdbVersionService;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/20
 */
@RestController
@RequestMapping(value = "/api/v2")
@Api( tags = "RDB版本映射管理接口")
@Validated
public class RdbController {
    @Autowired
    private IRdbVersionService rdbVersionService;
    /**
     * 获取RDB List
     * @return
     * @throws Exception
     */

    @ApiOperation(value="获取Redis版本与RDB映射版本列表", notes="获取Redis版本与RDB映射版本列表",httpMethod="POST")
    @RequestMapping(value = "/getRdbList",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap getRdbList()throws Exception{
        List<RdbVersionModel>rdbVersionModelList=rdbVersionService.selectAll();
        return ResultMap.builder().code("2000").data(rdbVersionModelList).msg("The request is successful");
    }



    @ApiResponses({
            @ApiResponse(code = 2000, message = "The request is successful", examples = @Example({
                    @ExampleProperty(value = "{'user':'id'}", mediaType = "application/json")
            })
            )
    })

//    @ApiImplicitParam(paramType = "header",name = "Syncer-Token",value ="token标记",dataType ="Integer"),
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "currentPage",value ="当前页码",dataType ="Integer"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value ="当前页数目",dataType ="Integer")
    })

    @ApiOperation(value="分页获取Redis版本与RDB映射版本列表", notes="分页获取Redis版本与RDB映射版本列表",httpMethod="POST")
    @RequestMapping(value = "/getRdbListByPage",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap getRdbListByPage(@RequestBody PageParamBean pageParamBean)throws Exception{
        if (pageParamBean.getCurrentPage()==0){
            pageParamBean.setCurrentPage(1);
        }
        if (pageParamBean.getPageSize()==0){
            pageParamBean.setPageSize(10);
        }
        PageBean<RdbVersionModel> rdbVersionModelList=rdbVersionService.findRdbVersionModelByPage(pageParamBean.getCurrentPage(),pageParamBean.getPageSize());
        return ResultMap.builder().code("2000").data(rdbVersionModelList).msg("The request is successful");
    }

    /**
     * RDB 新增
     * @return
     * @throws Exception
     */


    @ApiOperation(value="插入Redis版本与RDB映射版本信息", notes="新增Redis版本与RDB映射版本信息",httpMethod="POST")
    @RequestMapping(value = "/insetRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap insertRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{
        if(rdbVersionService.findRdbVersionModelByRedisVersionAndRdbVersion(rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version())!=null){
            return ResultMap.builder().code("2000").msg("当前映射关系已存在");
        }
        rdbVersionService.insertRdbVersionModel(rdbVersionModel);
        return ResultMap.builder().code("2000").msg("success");
    }

    @ApiOperation(value="更新Redis版本与RDB映射版本信息", notes="修改Redis版本与RDB映射版本信息",httpMethod="POST")
    @RequestMapping(value = "/updateRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap updateRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{
        RdbVersionModel dbRdbVersionModel=rdbVersionService.findRdbVersionModelById(rdbVersionModel.getId());
        if(dbRdbVersionModel==null){
            return ResultMap.builder().code("2000").msg("当前映射关系不存在");
        }
        rdbVersionService.updateRdbVersionModelById(dbRdbVersionModel.getId(),rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version());
        return ResultMap.builder().code("2000").msg("success");
    }

    @ApiOperation(value="删除Redis版本与RDB映射版本信息", notes="删除Redis版本与RDB映射版本信息",httpMethod="POST")
    @RequestMapping(value = "/deleteRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap deleteRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{
        rdbVersionService.deleteRdbVersionModelById(rdbVersionModel.getId());
        return ResultMap.builder().code("2000").msg("success");
    }
}
