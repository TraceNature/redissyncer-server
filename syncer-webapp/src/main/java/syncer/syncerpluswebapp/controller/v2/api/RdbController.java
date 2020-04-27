package syncer.syncerpluswebapp.controller.v2.api;

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
@Validated
public class RdbController {
    @Autowired
    private IRdbVersionService rdbVersionService;
    /**
     * 获取RDB List
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getRdbList",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap getRdbList()throws Exception{
        List<RdbVersionModel>rdbVersionModelList=rdbVersionService.selectAll();
        return ResultMap.builder().code("2000").data(rdbVersionModelList).msg("The request is successful");
    }

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
    @RequestMapping(value = "/insetRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap insertRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{
        if(rdbVersionService.findRdbVersionModelByRedisVersionAndRdbVersion(rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version())!=null){
            return ResultMap.builder().code("2000").msg("当前映射关系已存在");
        }
        rdbVersionService.insertRdbVersionModel(rdbVersionModel);
        return ResultMap.builder().code("2000").msg("success");
    }

    @RequestMapping(value = "/updateRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap updateRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{
        RdbVersionModel dbRdbVersionModel=rdbVersionService.findRdbVersionModelById(rdbVersionModel.getId());
        if(dbRdbVersionModel==null){
            return ResultMap.builder().code("2000").msg("当前映射关系不存在");
        }
        rdbVersionService.updateRdbVersionModelById(dbRdbVersionModel.getId(),rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version());
        return ResultMap.builder().code("2000").msg("success");
    }


    @RequestMapping(value = "/deleteRdbVersion",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap deleteRdbVersion(@RequestBody @Validated RdbVersionModel rdbVersionModel)throws Exception{
        rdbVersionService.deleteRdbVersionModelById(rdbVersionModel.getId());
        return ResultMap.builder().code("2000").msg("success");
    }
}
