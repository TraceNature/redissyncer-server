package syncer.syncerpluswebapp.config.submit;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import syncer.syncerpluscommon.entity.ResultMap;
import syncer.syncerpluscommon.util.md5.MD5Utils;
import syncer.syncerplusredis.entity.dto.FileCommandBackupDataDto;
import syncer.syncerplusredis.entity.dto.RedisClusterDto;
import syncer.syncerplusredis.entity.dto.RedisFileDataDto;
import syncer.syncerplusredis.entity.dto.task.TaskMsgDto;
import syncer.syncerplusredis.entity.dto.task.TaskStartMsgDto;

import java.lang.reflect.Method;

/**
 * @ClassName RequestDataAspect
 * @Description 数据重复提交校验
 * @Author lijing
 * @Date 2019/05/16 17:05
 **/
@Slf4j
@Aspect
@Component
public class ResubmitDataAspect {

    private final static String DATA = "data";
    private final static Object PRESENT = new Object();

    @Around("@annotation(syncer.syncerpluswebapp.config.submit.Resubmit)")
    public Object handleResubmit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //获取注解信息
        Resubmit annotation = method.getAnnotation(Resubmit.class);
        int delaySeconds = annotation.delaySeconds();
        Object[] pointArgs = joinPoint.getArgs();
        String key = "";
        //获取第一个参数
        Object firstParam = pointArgs[0];

        StringBuilder stringBuilder = new StringBuilder();
        if (firstParam instanceof RedisClusterDto) {
            //解析参数
            RedisClusterDto redisClusterDto = (RedisClusterDto) firstParam;
            if (!StringUtils.isEmpty(redisClusterDto.getSourceRedisAddress())) {
                stringBuilder.append(redisClusterDto.getSourceRedisAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

            if (!StringUtils.isEmpty(redisClusterDto.getSourcePassword())) {
                stringBuilder.append(redisClusterDto.getSourcePassword());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }


            if (!StringUtils.isEmpty(redisClusterDto.getTargetRedisAddress())) {
                stringBuilder.append(redisClusterDto.getTargetRedisAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

            if (!StringUtils.isEmpty(redisClusterDto.getTargetPassword())) {
                stringBuilder.append(redisClusterDto.getTargetPassword());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }


            if (!StringUtils.isEmpty(redisClusterDto.getFileAddress())) {
                stringBuilder.append(redisClusterDto.getFileAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

            if (!StringUtils.isEmpty(redisClusterDto.getTaskName())) {
                stringBuilder.append(redisClusterDto.getTaskName());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }


//            String md5= MD5Utils.getMD5(stringBuilder.toString());
//            JSONObject requestDTO = JSONObject.parseObject(firstParam.toString());
//            JSONObject data = JSONObject.parseObject(requestDTO.getString(DATA));
//            if (data != null) {
//                StringBuffer sb = new StringBuffer();
//                data.forEach((k, v) -> {
//                    sb.append(v);
//                });

        }else if(firstParam instanceof RedisFileDataDto){
            RedisFileDataDto redisFileDataDto= (RedisFileDataDto) firstParam;
            if (!StringUtils.isEmpty(redisFileDataDto.getFileAddress())) {
                stringBuilder.append(redisFileDataDto.getFileAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
            if (!StringUtils.isEmpty(redisFileDataDto.getTargetRedisAddress())) {
                stringBuilder.append(redisFileDataDto.getTargetRedisAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

            if (!StringUtils.isEmpty(redisFileDataDto.getTargetPassword())) {
                stringBuilder.append(redisFileDataDto.getTargetPassword());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
            if (!StringUtils.isEmpty(redisFileDataDto.getTaskName())) {
                stringBuilder.append(redisFileDataDto.getTaskName());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

        }else if(firstParam instanceof TaskStartMsgDto){
            TaskStartMsgDto params= (TaskStartMsgDto) firstParam;
            if (!StringUtils.isEmpty(params.getGroupId())) {
                stringBuilder.append(params.getGroupId());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
            if (!StringUtils.isEmpty(params.getTaskid())) {
                stringBuilder.append(params.getTaskid());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
        }else if(firstParam instanceof TaskMsgDto){
            TaskMsgDto params= (TaskMsgDto) firstParam;
            if (params.getGroupIds()!=null) {
                stringBuilder.append(JSON.toJSONString(params.getGroupIds()));
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
            if (null!=params.getTaskids()) {
                stringBuilder.append(JSON.toJSONString(params.getTaskids()));
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
        }else if(firstParam instanceof FileCommandBackupDataDto){
            FileCommandBackupDataDto params= (FileCommandBackupDataDto) firstParam;

            if (!StringUtils.isEmpty(params.getSourceRedisAddress())) {
                stringBuilder.append(params.getSourceRedisAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

            if (!StringUtils.isEmpty(params.getSourcePassword())) {
                stringBuilder.append(params.getSourcePassword());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }


            if (!StringUtils.isEmpty(params.getTaskName())) {
                stringBuilder.append(params.getTaskName());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
            if (!StringUtils.isEmpty(params.getFileAddress())) {
                stringBuilder.append(params.getFileAddress());
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

        }

        //生成加密参数 使用了content_MD5的加密方式
        key = ResubmitLock.handleKey(stringBuilder.toString());

        //执行锁
        boolean lock = false;
        try {
            //设置解锁key
            lock = ResubmitLock.getInstance().lock(key, PRESENT);
            if (lock) {
                //放行
                return joinPoint.proceed();
            } else {
                //响应重复提交异常
                return ResultMap.builder().code("101").msg("数据已提交");
            }
        } finally {
            //设置解锁key和解锁时间
            ResubmitLock.getInstance().unLock(lock, key, delaySeconds);
        }
    }
}