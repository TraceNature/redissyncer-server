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
import lombok.extern.slf4j.Slf4j;
import syncer.common.config.EtcdServerConfig;
import syncer.common.constant.StoreType;
import syncer.common.util.spring.SpringUtil;
import syncer.transmission.etcd.client.JEtcdClient;
import syncer.transmission.mapper.*;
import syncer.transmission.mapper.etcd.*;
import syncer.transmission.model.*;

import java.util.List;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/23
 */
@Slf4j
public class SqlOPUtils {
    static DashBoardMapper dashBoardMapper = null;
    static AbandonCommandMapper abandonCommandMapper = null;
    static BigKeyMapper bigKeyMapper = null;
    static DataCompensationMapper dataCompensationMapper = null;
    static RdbVersionMapper rdbVersionMapper = null;
    static TaskMapper taskMapper = null;
    static TaskOffsetMapper taskOffsetMapper = null;
    static UserMapper userMapper = null;
    static RubbishDataMapper rubbishDataMapper = null;
    static EtcdServerConfig config = new EtcdServerConfig();
    //    private static Lock lock=new ReentrantLock();
    static {
        EtcdServerConfig serverConfig = new EtcdServerConfig();
        if (StoreType.SQLITE.equals(serverConfig.getStoreType())) {
            abandonCommandMapper = SpringUtil.getBean(AbandonCommandMapper.class);
            bigKeyMapper = SpringUtil.getBean(BigKeyMapper.class);
            dataCompensationMapper = SpringUtil.getBean(DataCompensationMapper.class);
            rdbVersionMapper = SpringUtil.getBean(RdbVersionMapper.class);
            taskMapper = SpringUtil.getBean(TaskMapper.class);
            taskOffsetMapper = SpringUtil.getBean(TaskOffsetMapper.class);
            userMapper = SpringUtil.getBean(UserMapper.class);
            rubbishDataMapper = SpringUtil.getBean(RubbishDataMapper.class);
            dashBoardMapper = SpringUtil.getBean(DashBoardMapper.class);
        } else {
            JEtcdClient client = JEtcdClient.build();
            EtcdID etcdID= EtcdID.builder().client(client).nodeId(config.getNodeId()).build();
            abandonCommandMapper = EtcdAbandonCommandMapper.builder().client(client).etcdID(etcdID).nodeId(config.getNodeId()).build();
            bigKeyMapper = EtcdBigKeyMapper.builder().client(client).nodeId(config.getNodeId()).etcdID(etcdID).build();
            dataCompensationMapper = EtcdDataCompensationMapper.builder().client(client).nodeId(config.getNodeId()).etcdID(etcdID).build();
            rdbVersionMapper = EtcdRdbVersionMapper.builder().client(client).nodeId(config.getNodeId()).etcdID(etcdID).build();
            taskMapper = EtcdTaskMapper.builder().client(client).nodeId(config.getNodeId()).build();
            taskOffsetMapper = EtcdTaskOffsetMapper.builder().client(client).nodeId(config.getNodeId()).build();
            rubbishDataMapper = EtcdRubbishDataMapper.builder().client(client).nodeId(config.getNodeId()).build();
            userMapper = EtcdUserMapper.builder().client(client).nodeId(config.getNodeId()).build();
            dashBoardMapper = EtcdDashBoardMapper.builder().client(client).nodeId(config.getNodeId()).build();
        }

    }


//    static {
//
//        abandonCommandMapper= SpringUtil.getBean(AbandonCommandMapper.class);
//        bigKeyMapper= SpringUtil.getBean(BigKeyMapper.class);
//        dataCompensationMapper= SpringUtil.getBean(DataCompensationMapper.class);
//        rdbVersionMapper= SpringUtil.getBean(RdbVersionMapper.class);
//        taskMapper= SpringUtil.getBean(TaskMapper.class);
//        taskOffsetMapper= SpringUtil.getBean(TaskOffsetMapper.class);
//        userMapper= SpringUtil.getBean(UserMapper.class);
//        rubbishDataMapper=SpringUtil.getBean(RubbishDataMapper.class);

//    }


    public static boolean insertTask(TaskModel taskModel) throws Exception {
        return taskMapper.insertTask(taskModel);

    }


    public static boolean updateTaskOffsetById(String id, long offset) throws Exception {
        return taskMapper.updateTaskOffsetById(id, offset);
    }


    public static boolean updateTaskStatusById(String id, int status) throws Exception {
        return taskMapper.updateTaskStatusById(id, status);
    }


    public static TaskModel findTaskById(String id) throws Exception {

        return taskMapper.findTaskById(id);

    }


    public static List<TaskModel> findTaskByGroupId(String groupId) throws Exception {
        return taskMapper.findTaskByGroupId(groupId);
    }


    public static List<TaskModel> selectAll() throws Exception {
        return taskMapper.selectAll();
    }


    public static List<DataCompensationModel> findDataCompensationModelListByTaskId(String taskId) throws Exception {
        return dataCompensationMapper.findDataCompensationModelListByTaskId(taskId);
    }

    public static List<DataCompensationModel> findDataCompensationModelListByGroupId(String groupId) throws Exception {
        return dataCompensationMapper.findDataCompensationModelListByGroupId(groupId);
    }


    public List<AbandonCommandModel> findAbandonCommandListByTaskId(String taskId) throws Exception {
        return abandonCommandMapper.findAbandonCommandListByTaskId(taskId);
    }


    public List<AbandonCommandModel> findAbandonCommandListByGroupId(String groupId) throws Exception {
        return abandonCommandMapper.findAbandonCommandListByGroupId(groupId);
    }


    public boolean insertAbandonCommandModel(AbandonCommandModel abandonCommandModel) throws Exception {
        return insertAbandonCommandModel(abandonCommandModel);
    }

    public static List<TaskModel> findTaskBytaskName(String taskName) throws Exception {
        return taskMapper.findTaskBytaskName(taskName);
    }

    public static int countItem() throws Exception {
        return taskMapper.countItem();
    }

    public static int rdbCountItem() throws Exception {
        return rdbVersionMapper.countItem();
    }

    public static List<TaskModel> findTaskBytaskStatus(Integer status) throws Exception {
        return taskMapper.findTaskBytaskStatus(status);
    }

    public static boolean deleteTaskById(String id) throws Exception {
        return taskMapper.deleteTaskById(id);
    }

    public static boolean updateTaskMsgAndStatusById(Integer status, String taskMsg, String id) throws Exception {
        return taskMapper.updateTaskMsgAndStatusById(status, taskMsg, id);

    }

    public static boolean updateTaskMsgById(String taskMsg, String id) throws Exception {
        return taskMapper.updateTaskMsgById(taskMsg, id);
    }

    /**
     * 更新key记录
     * @param id
     * @param rdbKeyCount
     * @param allKeyCount
     * @param realKeyCount
     * @return
     * @throws Exception
     */
    public static boolean updateKeyCountById(String id, Long rdbKeyCount, Long allKeyCount, Long realKeyCount) throws Exception {

        return taskMapper.updateKeyCountById(id, rdbKeyCount, allKeyCount, realKeyCount);
    }

    public static boolean updateOffset(String id, Long offset) throws Exception {
        return taskMapper.updateOffset(id, offset);
    }

    public static boolean updateDataAnalysis(String id, String dataAnalysis) throws Exception {
        return taskMapper.updateDataAnalysis(id, dataAnalysis);
    }

    public static List<TaskModel> findTaskBytaskMd5(String md5) throws Exception {
        return taskMapper.findTaskBytaskMd5(md5);

    }


    public static boolean updateOffsetAndReplId(String id, Long offset, String replId) throws Exception {
        return taskMapper.updateOffsetAndReplId(id, offset, replId);
    }

    public static boolean updateAfreshsetById(String id, boolean afresh) throws Exception {
        return taskMapper.updateAfreshsetById(id, afresh);
    }

    public static boolean updateTask(TaskModel taskModel) throws Exception {
        return taskMapper.updateTask(taskModel);
    }

    public static boolean updateExpandTaskModelById(String id, String expandJson) throws Exception {
        return taskMapper.updateExpandTaskModelById(id, expandJson);
    }

    public static boolean insetTaskOffset(TaskOffsetModel taskOffsetModel) throws Exception {
        return taskOffsetMapper.insetTaskOffset(taskOffsetModel);
    }


    public static boolean updateOffsetByTaskId(String taskId, long offset) throws Exception {
        return taskOffsetMapper.updateOffsetByTaskId(taskId, offset);
    }


    public static boolean updateReplIdByTaskId(String taskId, String replId) throws Exception {
        return taskOffsetMapper.updateReplIdByTaskId(taskId, replId);
    }


    public static boolean updateOffsetAndReplIdByTaskId(String taskId, String replId, long offset) throws Exception {
        return taskOffsetMapper.updateOffsetAndReplIdByTaskId(taskId, replId, offset);
    }


    public static int delOffsetEntityByTaskId(String taskId) throws Exception {
        return taskOffsetMapper.delOffsetEntityByTaskId(taskId);
    }


    public static int delOffsetEntityByGroupId(String groupId) throws Exception {
        return taskOffsetMapper.delOffsetEntityByGroupId(groupId);
    }


    public static List<UserModel> findUserByUsername(String username) throws Exception {
        return userMapper.findUserByUsername(username);

    }


    public static boolean insertSimpleAbandonCommandModel(AbandonCommandModel abandonCommandModel) throws Exception {
        return abandonCommandMapper.insertSimpleAbandonCommandModel(abandonCommandModel);

    }


    public static void deleteAbandonCommandModelById(String id) throws Exception {
        abandonCommandMapper.deleteAbandonCommandModelById(id);
    }

    public static void deleteAbandonCommandModelByTaskId(String taskId) throws Exception {
        abandonCommandMapper.deleteAbandonCommandModelByTaskId(taskId);
    }


    public static void deleteAbandonCommandModelByGroupId(String groupId) throws Exception {
        abandonCommandMapper.deleteAbandonCommandModelByGroupId(groupId);
    }


    public static boolean insertDataCompensationModel(DataCompensationModel abandonCommandModel) throws Exception {
        return dataCompensationMapper.insertDataCompensationModel(abandonCommandModel);
    }

    public static void deleteDataCompensationModelById(String id) throws Exception {
        dataCompensationMapper.deleteDataCompensationModelById(id);
    }


    public static void deleteDataCompensationModelByTaskId(String taskId) throws Exception {
        dataCompensationMapper.deleteDataCompensationModelByTaskId(taskId);
    }


    public static void deleteDataCompensationModelByGroupId(String groupId) throws Exception {
        dataCompensationMapper.deleteDataCompensationModelByGroupId(groupId);
    }

    public static List<BigKeyModel> findBigKeyCommandListByTaskId(String taskId) throws Exception {
        return bigKeyMapper.findBigKeyCommandListByTaskId(taskId);
    }

    public static boolean insertBigKeyCommandModel(BigKeyModel bigKeyModel) throws Exception {

        return bigKeyMapper.insertBigKeyCommandModel(bigKeyModel);

    }


    public static void deleteBigKeyCommandModelById(String id) throws Exception {
        bigKeyMapper.deleteBigKeyCommandModelById(id);
    }


    public static void deleteBigKeyCommandModelByTaskId(String taskId) throws Exception {
        bigKeyMapper.deleteBigKeyCommandModelByTaskId(taskId);
    }


    public static void deleteBigKeyCommandModelByGroupId(String groupId) throws Exception {
        bigKeyMapper.deleteBigKeyCommandModelByGroupId(groupId);
    }

    public static List<RdbVersionModel> RdbVersionSelectAll() throws Exception {
        return rdbVersionMapper.findAllRdbVersion();
    }


    public static int RdbVersioncountItem() throws Exception {
        return rdbVersionMapper.countItem();
    }

    public static boolean deleteRdbVersionModelById(Integer id) throws Exception {

        return rdbVersionMapper.deleteRdbVersionModelById(id);

    }


    public static int deleteRdbVersionModelByRedisVersion(String redisVersion) throws Exception {
        return rdbVersionMapper.deleteRdbVersionModelByRedisVersion(redisVersion);
    }


    public static int deleteAllRdbVersionModel() {
        return rdbVersionMapper.deleteAllRdbVersionModel();
    }


    public static List<RdbVersionModel> findAllRdbVersion() throws Exception {
        return rdbVersionMapper.findAllRdbVersion();
    }

    public static RdbVersionModel findRdbVersionModelById(Integer id) throws Exception {
        return rdbVersionMapper.findRdbVersionModelById(id);
    }

    public static RdbVersionModel findRdbVersionModelByRedisVersionAndRdbVersion(String redisVersion, Integer rdbVersion) throws Exception {
        return rdbVersionMapper.findRdbVersionModelByRedisVersionAndRdbVersion(redisVersion, rdbVersion);
    }


    public static List<RdbVersionModel> findTaskByRdbVersion(Integer rdbVersion) throws Exception {
        return rdbVersionMapper.findTaskByRdbVersion(rdbVersion);
    }

    public static boolean insertRdbVersionModel(RdbVersionModel rdbVersionModel) throws Exception {
        return rdbVersionMapper.insertRdbVersionModel(rdbVersionModel);
    }

    public static boolean updateRdbVersionModelById(Integer id, String redisVersion, Integer rdbVersion) throws Exception {
        return rdbVersionMapper.updateRdbVersionModelById(id, redisVersion, rdbVersion);
    }


    public static int insertRdbVersionModelList(List<RdbVersionModel> rdbVersionModelList) {
        return rdbVersionMapper.insertRdbVersionModelList(rdbVersionModelList);
    }

    public static RdbVersionModel findRdbVersionModelByRedisVersion(String redisVersion) throws Exception {
        return rdbVersionMapper.findRdbVersionModelByRedisVersion(redisVersion);

    }

    public static void close() {
        try {
            if(Objects.nonNull(taskMapper)){
                taskMapper.close();
            }
        }catch (Exception e){
            log.error("etcd taskMapper close error {}",e.getMessage());
        }

    }


    /**
     * 清理垃圾数据
     */

    public static void deleteRubbishDataFromTaskOffSet() {
        rubbishDataMapper.deleteRubbishDataFromTaskOffSet();
    }


    public static void deleteRubbishDataFromTaskBigKey() {
        rubbishDataMapper.deleteRubbishDataFromTaskBigKey();
    }


    public static void deleteRubbishDataFromTaskDataMonitor() {
        rubbishDataMapper.deleteRubbishDataFromTaskDataMonitor();
    }


    public static void deleteRubbishDataFromTaskDataCompensation() {
        rubbishDataMapper.deleteRubbishDataFromTaskDataCompensation();
    }


    public static void deleteRubbishDataFromTaskDataAbandonCommand() {
        rubbishDataMapper.deleteRubbishDataFromTaskDataAbandonCommand();
    }


    /**
     * DASH
     * @return
     */

    public static int taskCount() {
        return dashBoardMapper.taskCount();
    }

    public static int brokenCount() {
        return dashBoardMapper.brokenCount();
    }


    public static int stopCount() {
        return dashBoardMapper.stopCount();
    }


    public static int runCount() {
        return dashBoardMapper.runCount();
    }


    public static int syncCount() {
        return dashBoardMapper.syncCount();
    }

    public static int rdbCount() {
        return dashBoardMapper.rdbCount();
    }

    public static int aofCount() {
        return dashBoardMapper.aofCount();
    }

    public static int mixedCount() {
        return dashBoardMapper.mixedCount();
    }

    public static int onlineRdbCount() {
        return dashBoardMapper.onlineRdbCount();
    }

    public static int onlineAofCount() {
        return dashBoardMapper.onlineAofCount();
    }

    public static int onlineMixedCount() {
        return dashBoardMapper.onlineMixedCount();
    }

    public int commandDumpUpCount() {
        return dashBoardMapper.commandDumpUpCount();
    }
}
