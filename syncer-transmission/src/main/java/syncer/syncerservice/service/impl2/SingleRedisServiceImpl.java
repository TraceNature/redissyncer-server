package syncer.syncerservice.service.impl2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerplusredis.constant.RedisStartCheckTypeEnum;
import syncer.syncerplusredis.constant.ThreadStatusEnum;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.RedisStartCheckEntity;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.thread.ThreadMsgEntity;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.util.TaskMsgUtils;
import syncer.syncerservice.service.IRedisTaskService;
import syncer.syncerservice.util.SyncTaskUtils;
import syncer.syncerservice.util.TaskCheckUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/2/25
 */
@Slf4j
public class SingleRedisServiceImpl implements IRedisTaskService {
    @Autowired
    RedisPoolProps redisPoolProps;

    @Override
    public ResultMap runSyncerTask(RedisStartCheckEntity redisStartCheckEntity, RedisStartCheckTypeEnum redisStartCheckType) throws TaskMsgException {




        return null;
    }
}
