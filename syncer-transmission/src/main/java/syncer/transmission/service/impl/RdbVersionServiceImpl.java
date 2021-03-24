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

package syncer.transmission.service.impl;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syncer.common.bean.PageBean;
import syncer.transmission.exception.TaskErrorException;
import syncer.transmission.mapper.RdbVersionMapper;
import syncer.transmission.model.RdbVersionModel;
import syncer.transmission.service.IRdbVersionService;
import syncer.transmission.util.sql.SqlOPUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/7
 */
@Service
public class RdbVersionServiceImpl implements IRdbVersionService {

    @Override
    public PageBean<RdbVersionModel> findRdbVersionModelByPage(int currentPage, int pageSize) throws Exception {
        PageHelper.startPage(currentPage, pageSize);
        List<RdbVersionModel> allItems = SqlOPUtils.findAllRdbVersion();
        int countNums = SqlOPUtils.rdbCountItem();
        PageBean<RdbVersionModel> pageData = new PageBean<>(currentPage, pageSize, countNums);
        pageData.setItems(allItems);
        return pageData;
    }

    @Override
    public List<RdbVersionModel> findAllRdbVersion() throws Exception {
        return SqlOPUtils.findAllRdbVersion();
    }

    @Override
    public RdbVersionModel findRdbVersionModelById(Integer id) throws Exception {
        return SqlOPUtils.findRdbVersionModelById(id);
    }

    @Override
    public boolean deleteRdbVersionModelById(Integer id) throws Exception {
        return SqlOPUtils.deleteRdbVersionModelById(id);
    }

    @Override
    public RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion, Integer rdbVersion) throws Exception {
        return SqlOPUtils.findRdbVersionModelByRedisVersionAndRdbVersion(redisVersion, rdbVersion);
    }

    @Override
    public boolean insertRdbVersion(RdbVersionModel rdbVersionModel) throws Exception {
        //判断是否已存在
        RdbVersionModel result=findRdbVersionModelByRedisVersionAndRdbVersion(rdbVersionModel.getRedis_version(),rdbVersionModel.getRdb_version());
        if(Objects.nonNull(result)){
            throw new TaskErrorException("当前映射关系已存在");
        }
        return SqlOPUtils.insertRdbVersionModel(rdbVersionModel);
    }

    @Override
    public boolean updateRdbVersionModelById(Integer id, String redisVersion, Integer rdbVersion) throws Exception {
        RdbVersionModel dbRdbVersionModel=findRdbVersionModelById(id);
        if(dbRdbVersionModel==null){
            throw new TaskErrorException("当前映射关系不存在");
        }
        return SqlOPUtils.updateRdbVersionModelById(dbRdbVersionModel.getId(),redisVersion,rdbVersion);
    }
}
