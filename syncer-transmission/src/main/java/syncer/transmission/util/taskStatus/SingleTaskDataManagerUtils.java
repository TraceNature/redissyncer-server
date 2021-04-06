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

package syncer.transmission.util.taskStatus;

import lombok.Getter;
import syncer.replica.status.TaskStatus;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.manger.ITaskStatusManger;
import syncer.transmission.util.manger.impl.MemoryAndSqliteTaskStatusManger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/14
 */

public class SingleTaskDataManagerUtils {
    static ITaskStatusManger statusManger=new MemoryAndSqliteTaskStatusManger();
    /**
     * Redis版本映射
     */
    @Getter
    public static final Map<String, Integer> RDB_VERSION_MAP=new ConcurrentHashMap<String,Integer>();
    @Getter
    private static Map<String, TaskDataEntity> aliveThreadHashMap=MemoryAndSqliteTaskStatusManger.getAliveThreadHashMap();
    /**
     *   "2": "6",
     *   "2.6": "6",
     *   "2.8": "6",
     *   "3": "6",
     *   "3.0": "6",
     *   "3.2": "7",
     *   "4.0": "8",
     *   "4": "8",
     *   "5.0": "9",
     *   "5": "9",
     *   "6": "9",
     *   "6.0": "9",
     *   "6.2": "9",
     *   "jimdb_3.2": "6",
     *   "jimdb_4.0": "6",
     *   "jimdb_4.1": "6",
     *   "jimdb_5.0": "6",
     *   "jimdb": "6"
     */
    static {
        RDB_VERSION_MAP.put("2",6);
        RDB_VERSION_MAP.put("2.6",6);
        RDB_VERSION_MAP.put("2.8",6);
        RDB_VERSION_MAP.put("3",6);
        RDB_VERSION_MAP.put("3.0",6);
        RDB_VERSION_MAP.put("3.2",7);
        RDB_VERSION_MAP.put("4.0",8);
        RDB_VERSION_MAP.put("4",8);
        RDB_VERSION_MAP.put("5.0",9);
        RDB_VERSION_MAP.put("5",9);
        RDB_VERSION_MAP.put("6",9);
        RDB_VERSION_MAP.put("6.0",9);
        RDB_VERSION_MAP.put("6.2",9);
        RDB_VERSION_MAP.put("jimdb_3.2",6);
        RDB_VERSION_MAP.put("jimdb_4.0",6);
        RDB_VERSION_MAP.put("jimdb_4.1",6);
        RDB_VERSION_MAP.put("jimdb_5.0",6);
        RDB_VERSION_MAP.put("jimdb",6);
    }

    public synchronized static void addMemThread(String taskId, TaskDataEntity taskDataEntity, boolean status) throws Exception {
        statusManger.addMemoryDbThread(taskId,taskDataEntity,status);
    }

    public synchronized static void changeThreadStatus(String taskId, Long offset, TaskStatus taskType) throws Exception{
        statusManger.changeThreadStatus(taskId,offset,taskType);
    }

    public synchronized static void brokenTask(String taskId) throws Exception {
        statusManger.brokenTask(taskId);
    }

    public synchronized static void addDbThread(String taskId, TaskModel taskModel) throws Exception {
        statusManger.addDbThread(taskId,taskModel);
    }

    /**
     * 把任务信息加入内存
     * @param taskId
     * @param taskDataEntity
     * @throws Exception
     */
    public synchronized static void addMemThread(String taskId, TaskDataEntity taskDataEntity) throws Exception {
        statusManger.addDbThread(taskId,taskDataEntity);
    }

    /**
     * 更新状态
     * @param taskId
     * @param taskStatusType
     * @throws Exception
     */
    public synchronized static void updateThreadStatus(String taskId,TaskStatus taskStatusType) throws Exception {
        statusManger.updateThreadStatus(taskId,taskStatusType);
    }

    public static synchronized boolean isTaskClose(String taskId){
        return statusManger.isTaskClose(taskId);
    }


    public static synchronized void brokenStatusAndLog(Exception e, Class clazz, String taskId){
        statusManger.brokenStatusAndLog(e,clazz,taskId);
    }

    public static synchronized void brokenStatusAndLog(String exceptionMsg, Class clazz, String taskId){
        statusManger.brokenStatusAndLog(exceptionMsg,clazz,taskId);
    }

    public synchronized static void updateThreadStatusAndMsg(String taskId,String msg,TaskStatus taskStatusType) throws Exception{
        statusManger.updateThreadStatusAndMsg(taskId,msg,taskStatusType);
    }

    public synchronized static void updateThreadMsg(String taskId,String msg) throws Exception{
        statusManger.updateThreadMsg(taskId,msg);
    }
}
