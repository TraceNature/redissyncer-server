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

package syncer.transmission.util.sql;

import syncer.common.util.spring.SpringUtil;
import syncer.transmission.mapper.*;
import syncer.transmission.model.*;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/23
 */

public class SqlOPUtils {
     static AbandonCommandMapper abandonCommandMapper=null;
    static BigKeyMapper bigKeyMapper=null;
    static DataCompensationMapper dataCompensationMapper=null;
    static RdbVersionMapper rdbVersionMapper=null;
    static TaskMapper taskMapper=null;
    static TaskOffsetMapper taskOffsetMapper=null;
    static UserMapper userMapper=null;
//    private static Lock lock=new ReentrantLock();



    static {

        abandonCommandMapper= SpringUtil.getBean(AbandonCommandMapper.class);
        bigKeyMapper= SpringUtil.getBean(BigKeyMapper.class);
        dataCompensationMapper= SpringUtil.getBean(DataCompensationMapper.class);
        rdbVersionMapper= SpringUtil.getBean(RdbVersionMapper.class);
        taskMapper= SpringUtil.getBean(TaskMapper.class);
        taskOffsetMapper= SpringUtil.getBean(TaskOffsetMapper.class);
        userMapper= SpringUtil.getBean(UserMapper.class);
    }


    public static  boolean insertTask(TaskModel taskModel)throws Exception{
        return taskMapper.insertTask(taskModel);

    }


    public static   boolean updateTaskOffsetById(String id, long offset)throws Exception{
        return taskMapper.updateTaskOffsetById(id, offset);
    }


    public static  boolean updateTaskStatusById(String id,int status)throws Exception{
        return taskMapper.updateTaskStatusById(id, status);
    }


    public static  TaskModel findTaskById(String id)throws Exception{

        return taskMapper.findTaskById(id);

    }


    public static  List<TaskModel> findTaskByGroupId(String groupId)throws  Exception{
        return taskMapper.findTaskByGroupId(groupId);
    }


    public static  List<TaskModel> selectAll()throws Exception{
        return taskMapper.selectAll();
    }

    public static   List<TaskModel> findTaskBytaskName(String taskName)throws Exception{
        return taskMapper.findTaskBytaskName(taskName);
    }

    public static  int countItem()throws Exception{
        return taskMapper.countItem();
    }

    public static  List<TaskModel> findTaskBytaskStatus(Integer status)throws Exception{
        return taskMapper.findTaskBytaskStatus(status);
    }

    public static  boolean deleteTaskById(String id)throws Exception{
        return taskMapper.deleteTaskById(id);
    }
    public static  boolean updateTaskMsgAndStatusById(Integer status,String taskMsg,String id)throws Exception{
        return taskMapper.updateTaskMsgAndStatusById(status, taskMsg, id);

    }

    public static  boolean updateTaskMsgById(String taskMsg,String id)throws Exception{
        return taskMapper.updateTaskMsgById(taskMsg, id);
    }

    public static  boolean updateKeyCountById(String id,Long rdbKeyCount,Long allKeyCount,Long realKeyCount)throws Exception{

        return taskMapper.updateKeyCountById(id, rdbKeyCount, allKeyCount, realKeyCount);
    }

    public static   boolean updateOffset(String id,Long offset)throws Exception{
        return taskMapper.updateOffset(id, offset);
    }

    public static   boolean updateDataAnalysis(String id,String dataAnalysis)throws Exception{
        return taskMapper.updateDataAnalysis(id, dataAnalysis);
    }

    public static   List<TaskModel> findTaskBytaskMd5( String md5)throws Exception{
        return taskMapper.findTaskBytaskMd5(md5);

    }


    public static   boolean updateOffsetAndReplId(String id,Long offset,String replId)throws Exception{
        return taskMapper.updateOffsetAndReplId(id, offset, replId);
    }

    public static   boolean updateAfreshsetById(String id,boolean afresh)throws Exception{
        return taskMapper.updateAfreshsetById(id, afresh);
    }

    public static  boolean updateTask(TaskModel taskModel)throws Exception{
        return taskMapper.updateTask(taskModel);
    }

    public static boolean updateExpandTaskModelById(String id,String expandJson)throws Exception{
        return taskMapper.updateExpandTaskModelById(id,expandJson);
    }

    public static  boolean insetTaskOffset(TaskOffsetModel taskOffsetModel)throws Exception{
        return taskOffsetMapper.insetTaskOffset(taskOffsetModel);
    }



    public static   List<UserModel> findUserByUsername(String username)throws Exception{

        return userMapper.findUserByUsername(username);

    }


    public static  boolean insertSimpleAbandonCommandModel(AbandonCommandModel abandonCommandModel)throws Exception{
         return abandonCommandMapper.insertSimpleAbandonCommandModel(abandonCommandModel);

    }




    public static  boolean insertDataCompensationModel(DataCompensationModel abandonCommandModel)throws Exception{

        return dataCompensationMapper.insertDataCompensationModel(abandonCommandModel);

    }


    public static  boolean insertBigKeyCommandModel(BigKeyModel bigKeyModel)throws Exception{

        return bigKeyMapper.insertBigKeyCommandModel(bigKeyModel);

    }

    public static   List<RdbVersionModel> RdbVersionSelectAll()throws Exception{
        return rdbVersionMapper.findAllRdbVersion();
    }


    public static  int RdbVersioncountItem()throws Exception{

        return rdbVersionMapper.countItem();

    }

    public static   boolean deleteRdbVersionModelById(Integer id)throws Exception{

        return rdbVersionMapper.deleteRdbVersionModelById(id);

    }

    public static  RdbVersionModel findRdbVersionModelById( Integer id)throws Exception{

        return rdbVersionMapper.findRdbVersionModelById(id);

    }

    public static  RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion,Integer rdbVersion)throws Exception{

        return rdbVersionMapper.findRdbVersionModelByRedisVersionAndRdbVersion(redisVersion, rdbVersion);

    }

    public static   boolean insertRdbVersionModel(RdbVersionModel rdbVersionModel)throws Exception{

        return rdbVersionMapper.insertRdbVersionModel(rdbVersionModel);

    }

    public static   boolean updateRdbVersionModelById(Integer id,String redisVersion,Integer rdbVersion)throws Exception{

        return rdbVersionMapper.updateRdbVersionModelById(id, redisVersion, rdbVersion);

    }

    public static RdbVersionModel findRdbVersionModelByRedisVersion(String redisVersion)throws Exception{
        return rdbVersionMapper.findRdbVersionModelByRedisVersion( redisVersion);

    }
}
