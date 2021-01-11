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

package syncer.transmission.mapper;
import org.apache.ibatis.annotations.*;
import syncer.transmission.model.TaskModel;

import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/10
 */
//@Component
@Mapper
public interface TaskMapper {
    // 根据 ID 查询
    @Select("SELECT * FROM t_task order by createTime desc")
    List<TaskModel> selectAll()throws Exception;

    @Select("SELECT * FROM t_task WHERE id =#{id} order by createTime desc limit 1")
    TaskModel findTaskById(@Param("id") String id)throws Exception;

    @Select("select count(*) from t_task")
    int countItem()throws Exception;

    @Select("SELECT * FROM t_task WHERE taskName =#{taskName} order by createTime desc")
    List<TaskModel> findTaskBytaskName(@Param("taskName") String taskName)throws Exception;

    @Select("SELECT * FROM t_task WHERE md5 =#{md5}")
    List<TaskModel> findTaskBytaskMd5(@Param("md5") String md5)throws Exception;

    @Select("SELECT * FROM t_task WHERE status =#{status} order by createTime desc")
    List<TaskModel> findTaskBytaskStatus(@Param("status") Integer status)throws Exception;

    @Select("SELECT * FROM t_task WHERE groupId =#{groupId}")
    List<TaskModel>findTaskByGroupId(@Param("groupId") String groupId)throws  Exception;

    @Insert("INSERT INTO t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status,redisVersion,rdbVersion,sourceRedisType,targetRedisType,syncType,dbMapper,md5,replId,fileAddress,sourceAcl,targetAcl,sourceUserName,targetUserName,errorCount) VALUES(#{id}, #{groupId},#{taskName},#{sourceRedisAddress},#{sourcePassword},#{targetRedisAddress},#{targetPassword},#{autostart},#{afresh},#{batchSize},#{tasktype},#{offsetPlace},#{taskMsg},#{offset},#{status},#{redisVersion},#{rdbVersion},#{sourceRedisType},#{targetRedisType},#{syncType},#{dbMapper},#{md5},#{replId},#{fileAddress},#{sourceAcl},#{targetAcl},#{sourceUserName},#{targetUserName},#{errorCount})")
    boolean insertTask(TaskModel taskModel)throws Exception;


    @Insert({
            "<script>",
            "insert into t_task(id, groupId,taskName,sourceRedisAddress,sourcePassword,targetRedisAddress,targetPassword,autostart,afresh,batchSize,tasktype,offsetPlace,taskMsg,offset,status,redisVersion,rdbVersion,sourceRedisType,targetRedisType,syncType,dbMapper,md5,replId,fileAddress,sourceAcl,targetAcl,sourceUserName,targetUserName,errorCount) values ",
            "<foreach collection='taskModelList' item='item' index='index' separator=','>",
            "(#{item.id}, #{item.groupId}, #{item.taskName}, #{item.sourceRedisAddress}, #{item.sourcePassword}, #{item.targetRedisAddress}, #{item.targetPassword}, #{item.autostart}, #{item.afresh}, #{item.batchSize}, #{item.tasktype}, #{item.offsetPlace}, #{item.taskMsg}, #{item.offset}, #{item.status},#{item.redisVersion},#{item.rdbVersion},#{item.sourceRedisType},#{item.targetRedisType},#{item.syncType},#{item.dbMapper},#{item.md5},#{item.replId},#{item.fileAddress},,#{item.sourceAcl},#{item.targetAcl},#{item.sourceUserName},#{item.targetUserName},#{item.errorCount})",
            "</foreach>",
            "</script>"
    })
    int insertTaskList(@Param(value = "taskModelList") List<TaskModel> taskModelList);


    @Delete("DELETE FROM t_task WHERE id=#{id}")
    boolean deleteTaskById(@Param("id") String id)throws Exception;

    @Delete("DELETE FROM t_task WHERE groupId=#{groupId}")
    int deleteTasksByGroupId(@Param("groupId") String groupId)throws Exception;


    @Delete("DELETE FROM t_task")
    int deleteAllTask();

    @Update("UPDATE t_task SET groupId=#{groupId} ,taskName=#{taskName},sourceRedisAddress=#{sourceRedisAddress},sourcePassword=#{sourcePassword},targetRedisAddress=#{targetRedisAddress},targetPassword=#{targetPassword},autostart=#{autostart},afresh=#{afresh},batchSize=#{batchSize},tasktype=#{tasktype},offsetPlace=#{offsetPlace},taskMsg=#{taskMsg},offset=#{offset},status=#{status},redisVersion=#{redisVersion},rdbVersion=#{rdbVersion} ,targetRedisType=#{targetRedisType},sourceRedisType=#{sourceRedisType},syncType=#{syncType},dbMapper=#{dbMapper},updateTime=(datetime('now', 'localtime')) ,md5=#{md5},sourceAcl=#{sourceAcl},targetAcl=#{targetAcl},sourceUserName=#{sourceUserName},targetUserName=#{targetUserName},errorCount=#{errorCount} WHERE id=#{id}")
    boolean updateTask(TaskModel taskModel)throws Exception;

    @Update("UPDATE t_task SET status=#{status},updateTime=(datetime('now', 'localtime')) WHERE id=#{id}")
    boolean updateTaskStatusById(@Param("id") String id, @Param("status") int status)throws Exception;

    @Update("UPDATE t_task SET status=#{status},updateTime=(datetime('now', 'localtime')) WHERE groupId=#{groupId}")
    boolean updateTaskStausByGroupId(@Param("groupId") String groupId, @Param("status") int status)throws Exception;


    @Update("UPDATE t_task SET offset=#{offset},updateTime=(datetime('now', 'localtime')) WHERE id=#{id}")
    boolean updateTaskOffsetById(@Param("id") String id, @Param("offset") long offset)throws Exception;


    @Update("UPDATE t_task SET afresh=#{afresh},updateTime=(datetime('now', 'localtime')) WHERE id=#{id}")
    boolean updateAfreshsetById(@Param("id") String id, @Param("afresh") boolean afresh)throws Exception;


    @Update("UPDATE t_task SET status=#{status},taskMsg=#{taskMsg},updateTime=(datetime('now', 'localtime')) WHERE id=#{id}")
    boolean updateTaskMsgAndStatusById(@Param("status") Integer status, @Param("taskMsg") String taskMsg, @Param("id") String id)throws Exception;

    @Update("UPDATE t_task SET taskMsg=#{taskMsg},updateTime=(datetime('now', 'localtime')) WHERE id=#{id}")
    boolean updateTaskMsgById(@Param("taskMsg") String taskMsg, @Param("id") String id)throws Exception;

    @Update("UPDATE t_task set updateTime=(datetime('now', 'localtime')) where id=#{id}")
    boolean updateTime(@Param("id") String id)throws Exception;


    @Update("UPDATE t_task set offset=#{offset} where id=#{id}")
    boolean updateOffset(@Param("id") String id, @Param("offset") Long offset)throws Exception;

    @Update("UPDATE t_task set offset=#{offset},replId=#{replId} where id=#{id}")
    boolean updateOffsetAndReplId(@Param("id") String id, @Param("offset") Long offset, @Param("replId") String replId)throws Exception;

    @Update("UPDATE t_task set offset=#{offset},replId=#{replId},allKeyCount=#{allKeyCount},realKeyCount=#{realKeyCount} where id=#{id}")
    boolean updateOffsetAndReplIdAndAllKey(@Param("id") String id, @Param("offset") Long offset, @Param("replId") String replId, String allKeyCount, String realKeyCount)throws Exception;


    @Update("UPDATE t_task set dataAnalysis=#{dataAnalysis} where id=#{id}")
    boolean updateDataAnalysis(@Param("id") String id, @Param("dataAnalysis") String dataAnalysis)throws Exception;

    @Update("UPDATE t_task set rdbKeyCount=#{rdbKeyCount} where id=#{id}")
    boolean updateRdbKeyCountById(@Param("id") String id, @Param("rdbKeyCount") Long rdbKeyCount)throws Exception;

    @Update("UPDATE t_task set realKeyCount=#{realKeyCount} where id=#{id}")
    boolean updateRealKeyCountById(@Param("id") String id, @Param("realKeyCount") Long realKeyCount)throws Exception;

    @Update("UPDATE t_task set allKeyCount=#{allKeyCount} where id=#{id}")
    boolean updateAllKeyCountById(@Param("id") String id, @Param("allKeyCount") Long allKeyCount)throws Exception;

    @Update("UPDATE t_task set  rdbKeyCount=#{rdbKeyCount}, allKeyCount=#{allKeyCount},realKeyCount=#{realKeyCount} where id=#{id}")
    boolean updateKeyCountById(@Param("id") String id, @Param("rdbKeyCount") Long rdbKeyCount, @Param("allKeyCount") Long allKeyCount, @Param("realKeyCount") Long realKeyCount)throws Exception;

    @Update("UPDATE t_task set  expandJson=#{expandJson} where id=#{id}")
    boolean updateExpandTaskModelById(@Param("id") String id, @Param("expandJson") String expandJson);

//    @Update({
//            "<script>",
//            "<foreach collection='taskModelList' item='item' index='index' >",
//            "UPDATE t_task SET groupId=#{item.groupId} ,taskName=#{item.taskName},sourceRedisAddress=#{item.sourceRedisAddress},sourcePassword=#{item.sourcePassword},targetRedisAddress=#{item.targetRedisAddress},targetPassword=#{item.targetPassword},autostart=#{item.autostart},afresh=#{item.afresh},batchSize=#{item.batchSize},tasktype=#{item.tasktype},offsetPlace=#{item.offsetPlace},taskMsg=#{item.taskMsg},offset=#{item.offset},status=#{item.status} WHERE id=#{item.id};",
//            "</foreach>",
//            "</script>"
//    })

//        @Update({
//                "<script>",
//                "<foreach collection='taskModelList' item='item' index='index' open='' close='' separator=';/r/n'>",
//                "UPDATE t_task SET <if test='item.groupId!=null'> groupId=#{item.groupId} , </if>  <if test='item.taskName!=null'>taskName=#{item.taskName},</if> <if test='item.sourceRedisAddress!=null'>sourceRedisAddress=#{item.sourceRedisAddress},</if> <if test='item.sourcePassword!=null'> sourcePassword=#{item.sourcePassword}, </if>  <if test='item.targetRedisAddress!=null'> targetRedisAddress=#{item.targetRedisAddress}, </if>  <if test='item.targetPassword!=null'> targetPassword=#{item.targetPassword},  </if>   <if test='item.autostart!=null'>  autostart=#{item.autostart}, </if> <if test='item.afresh!=null'>  afresh=#{item.afresh}, </if>  <if test='item.batchSize!=null'> batchSize=#{item.batchSize}, </if> <if test='item.tasktype!=null'> tasktype=#{item.tasktype}, </if> <if test='item.offsetPlace!=null'> offsetPlace=#{item.offsetPlace}, </if> <if test='item.taskMsg!=null'> taskMsg=#{item.taskMsg}, </if>  <if test='item.offset!=null'> offset=#{item.offset},</if>  <if test='item.status!=null'> status=#{item.status} </if> WHERE id=#{item.id}",
//                "</foreach>",
//                "</script>"
//        })
//        Integer  updateTaskList(@Param(value="taskModelList")List<TaskModel>taskModelList)throws Exception;

}
