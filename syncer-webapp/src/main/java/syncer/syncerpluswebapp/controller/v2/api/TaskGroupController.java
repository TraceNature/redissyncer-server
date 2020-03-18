package syncer.syncerpluswebapp.controller.v2.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.exception.TaskMsgException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/14
 */
@RestController
@RequestMapping(value = "/api/v1")
@Validated

public class TaskGroupController {
    @Autowired
    RedisPoolProps redisPoolProps;


    /**
     * 创建同步任务
     * @param redisClusterDto
     * @return
     * @throws TaskMsgException
     */
    @RequestMapping(value = "/createtask",method = {RequestMethod.POST},produces="application/json;charset=utf-8;")
    public ResultMap createTask(@RequestBody @Validated RedisClusterDto redisClusterDto) throws TaskMsgException {

        return null;

    }


}
