package com.i1314i.syncerplusservice.util.code;

import com.alibaba.fastjson.JSON;
import com.i1314i.syncerpluscommon.entity.ResultMap;
import com.i1314i.syncerplusservice.constant.TaskMsgConstant;
import org.springframework.util.StringUtils;

public class CodeUtils {


    /**
     * 将ResultMap序列化生成字符串
     * @param code
     * @param msg
     * @param data
     * @return
     */
    public synchronized static String codeMessages(String code ,String msg,Object data){
        ResultMap resultMap=ResultMap.builder().code(code);
        if(!StringUtils.isEmpty(msg)){
            resultMap.msg(msg);
        }

        if(data!=null){
            resultMap.data(data);
        }

        return JSON.toJSONString(resultMap);
    }


    public synchronized static String codeMessages(String code ,String msg){
        return codeMessages(code,msg,null);
    }


    /**
     * 将字符串生成ResultMap
     */

    public synchronized static ResultMap codeMessages(String data){
        ResultMap resultMap;
        try {
            resultMap=JSON.parseObject(data,ResultMap.class);
        }catch (Exception e){
            resultMap=ResultMap.builder().code(TaskMsgConstant.TASK_MSG_SYSTEM_ERROR_CODE).msg(TaskMsgConstant.TASK_MSG_SYSTEM_ERROR);
        }
        return resultMap;
    }
}
