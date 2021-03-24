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
import syncer.transmission.model.TaskOffsetModel;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */
@Mapper
public interface TaskOffsetMapper {
    @Insert("INSERT INTO t_task_offset(taskId,offset,replId) VALUES(#{taskId},#{offset},#{replId})")
    boolean insetTaskOffset(TaskOffsetModel taskOffsetModel)throws Exception;

    @Update("UPDATE t_task_offset SET offset=#{offset} WHERE taskId=#{taskId}")
    boolean updateOffsetByTaskId(@Param("taskId") String taskId, @Param("offset") long offset)throws Exception;

    @Update("UPDATE t_task_offset SET replId=#{replId} WHERE taskId=#{taskId}")
    boolean updateReplIdByTaskId(@Param("taskId") String taskId, @Param("replId") String replId)throws Exception;


    @Update("UPDATE t_task_offset SET replId=#{replId},offset=#{offset} WHERE taskId=#{taskId}")
    boolean updateOffsetAndReplIdByTaskId(@Param("taskId") String taskId, @Param("replId") String replId, @Param("offset") long offset)throws Exception;

    @Delete("DELETE FROM t_task_offset WHERE taskId=#{taskId}")
    int delOffsetEntityByTaskId(@Param("taskId") String taskId)throws Exception;

    @Delete("DELETE FROM t_task_offset WHERE groupId=#{groupId}")
    int delOffsetEntityByGroupId(@Param("groupId") String groupId)throws Exception;
}
