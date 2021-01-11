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

package syncer.transmission.service;

import syncer.common.bean.PageBean;
import syncer.transmission.model.RdbVersionModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description RDB版本映射
 * @Date 2020/12/7
 */
public interface IRdbVersionService {

    /**
     * 分页查询RDB版本
     * @param currentPage
     * @param pageSize
     * @return
     * @throws Exception
     */
    PageBean<RdbVersionModel> findRdbVersionModelByPage(int currentPage, int pageSize) throws Exception;

    /**
     * 查询所有RDB版本
     * @return
     * @throws Exception
     */
    List<RdbVersionModel> findAllRdbVersion()throws Exception;

    /**
     *根据ID查询RDB版本
     * @param id
     * @return
     * @throws Exception
     */
    RdbVersionModel findRdbVersionModelById(Integer id)throws Exception;

    /**
     * 根据ID删除RDB版本
     * @param id
     * @return
     * @throws Exception
     */
    boolean deleteRdbVersionModelById(Integer id)throws Exception;

    /**
     * 根据Redis版本和RDB版本查询
     * @param redisVersion
     * @param rdbVersion
     * @return
     * @throws Exception
     */
    RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion,Integer rdbVersion)throws Exception;

    /**
     * 插入RDB版本
     * @param rdbVersionModel
     * @return
     * @throws Exception
     */
    boolean insertRdbVersion(RdbVersionModel rdbVersionModel)throws Exception;

    /**
     * 更新RDB版本
     * @param id
     * @param redisVersion
     * @param rdbVersion
     * @return
     * @throws Exception
     */
    boolean updateRdbVersionModelById(Integer id, String redisVersion,Integer rdbVersion)throws Exception;
}
