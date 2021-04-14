package syncer.transmission.checkpoint.breakpoint;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.config.BreakPointConfig;
import syncer.common.constant.BreakpointContinuationType;
import syncer.common.util.RegexUtil;
import syncer.jedis.Jedis;
import syncer.transmission.client.impl.JedisMultiExecPipeLineClient;
import syncer.transmission.entity.OffSetEntity;
import syncer.transmission.entity.TaskDataEntity;
import syncer.transmission.model.TaskModel;
import syncer.transmission.util.taskStatus.SingleTaskDataManagerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 断点续传
 */
@Slf4j
public class BreakPoint {
    private final static String REDIS_SYNCER_CHECKPOINT = "redis-syncer-checkpoint";

    public OffSetEntity checkPointOffset(TaskModel taskModel) {
        OffSetEntity offset = null;
        if (BreakPointConfig.getBreakpointContinuationType().equals(BreakpointContinuationType.v2)) {
            //单机版本
            if (taskModel.getTargetRedisType().equals(1)) {
                offset = getTargetPointOffset(taskModel.getTaskId()
                        , taskModel.getTargetHost()
                        , taskModel.getTargetPort()
                        , taskModel.getTargetPassword()
                        , taskModel.getSourceHost()
                        , taskModel.getSourcePort());
                if(Objects.isNull(offset)){
                    offset = getMemCheckPointOffset(taskModel.getTaskId());
                }
            } else {
                offset = getMemCheckPointOffset(taskModel.getTaskId());
            }

        } else {
            offset = getMemCheckPointOffset(taskModel.getTaskId());
        }
        return offset;
    }

    private OffSetEntity getTargetPointOffset(String taskId, String host, int port, String password, String sourceHost, int sourcePort) {
        Jedis client = null;
        try {
            client = new Jedis(host, port);
            if (!StringUtils.isEmpty(password)) {
                client.auth(password);
            }
            String infoRes = client.info("Keyspace");
            String rgex = "db(.*?):keys=(.*?),";
            List<List<String>> res = RegexUtil.getSubListUtil(infoRes, rgex, 2);
            List<Integer> dbList = res.stream().map(data -> {
                return Integer.parseInt(data.get(0));
            }).collect(Collectors.toList());

            if (Objects.isNull(dbList)) {

                return null;
            }
            String hostName = sourceHost + ":" + sourceHost;
            String offsetName = hostName + "-offset";
            String runidName = hostName + "-runid";
            String versionName = hostName + "-version";
            String runId = null;
            long maxOffset = -1L;
            for (int i = 0; i < dbList.size(); i++) {
                client.select(dbList.get(i));
                String boyRunId = client.hget(REDIS_SYNCER_CHECKPOINT, runidName);
                if (Objects.isNull(boyRunId)) {
                    continue;
                }
                runId = boyRunId;
                long offset = Long.parseLong(client.hget(REDIS_SYNCER_CHECKPOINT, offsetName));
                if (maxOffset < offset) {
                    maxOffset = offset;
                }
            }

            if (maxOffset == -1L) {
                return null;
            }
            log.info("TASKID[{}] v2 offset {} {} get success form dbList {}",taskId,runId,maxOffset,JSON.toJSONString(dbList));
            OffSetEntity result = OffSetEntity.builder().replId(runId).build();
            result.getReplOffset().set(maxOffset);
            return result;
        } catch (Exception e) {
            log.error("TASKID[{}] breakpoint get Keyspace fail,result [{}] ", taskId, e.getMessage());
        } finally {
            if (Objects.nonNull(client)) {
                client.close();
            }
        }
        return null;
    }

    private OffSetEntity getMemCheckPointOffset(String taskId) {
        TaskDataEntity taskDataEntity = SingleTaskDataManagerUtils.getAliveThreadHashMap().get(taskId);

        if (Objects.nonNull(taskDataEntity)) {
            log.info("TASKID[{}] v1 offset {} {} get success",taskId,taskDataEntity.getOffSetEntity().getReplId(),taskDataEntity.getOffSetEntity().getReplOffset().get());
            return taskDataEntity.getOffSetEntity();
        }
        return null;
    }
}
