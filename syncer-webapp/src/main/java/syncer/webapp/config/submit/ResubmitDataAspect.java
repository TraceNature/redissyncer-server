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

package syncer.webapp.config.submit;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import syncer.common.entity.ResponseResult;
import syncer.webapp.request.CreateFileTaskParam;
import syncer.webapp.request.CreateTaskParam;
import syncer.webapp.request.StartTaskParam;
import syncer.webapp.request.StopTaskParam;


import java.lang.reflect.Method;
import java.util.Objects;

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


    @Around("@annotation(syncer.webapp.config.submit.Resubmit)")
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
        if (firstParam instanceof CreateTaskParam) {
            //解析参数
            CreateTaskParam redisClusterDto = (CreateTaskParam) firstParam;
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

        }else if(firstParam instanceof StartTaskParam){
            StartTaskParam params= (StartTaskParam) firstParam;
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
        }else if(firstParam instanceof StopTaskParam){
            StopTaskParam params= (StopTaskParam) firstParam;
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
        }else if(firstParam instanceof CreateFileTaskParam){
            CreateFileTaskParam params= (CreateFileTaskParam) firstParam;
            if (params.getFileAddress()!=null) {
                stringBuilder.append(JSON.toJSONString(params.getFileAddress()));
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }
            if (Objects.nonNull(params.getTargetRedisAddress())) {
                stringBuilder.append(JSON.toJSONString(params.getTargetRedisAddress()));
                stringBuilder.append("-");
            } else {
                stringBuilder.append("null");
                stringBuilder.append("-");
            }

            if (!StringUtils.isEmpty(params.getTargetPassword())) {
                stringBuilder.append(params.getTargetPassword());
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
                return ResponseResult.builder().code("101").msg("数据已提交").build();
            }
        } finally {
            //设置解锁key和解锁时间
            ResubmitLock.getInstance().unLock(lock, key, delaySeconds);
        }
    }


}