package syncer.transmission.util.code;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;
import syncer.common.entity.ResponseResult;
import syncer.transmission.constants.TaskMsgConstant;

public class CodeUtils {

    /**
     * 将ResponseResult序列化生成字符串
     * @param code
     * @param msg
     * @param data
     * @return
     */
    public synchronized static String codeMessages(String code ,String msg,Object data){
        ResponseResult responseResult=ResponseResult.builder().code(code).build();
        if(!StringUtils.isEmpty(msg)){
            responseResult.setMsg(msg);
        }

        if(data!=null){
            responseResult.setData(data);
        }

        return JSON.toJSONString(responseResult);
    }


    public synchronized static String codeMessages(String code ,String msg){
        return codeMessages(code,msg,null);
    }


    /**
     * 将字符串生成ResultMap
     */

    public synchronized static ResponseResult codeMessages(String data){
        ResponseResult result;
        try {
            result=JSON.parseObject(data,ResponseResult.class);
        }catch (Exception e){
            result=ResponseResult.builder().code(TaskMsgConstant.TASK_MSG_SYSTEM_ERROR_CODE).msg(TaskMsgConstant.TASK_MSG_SYSTEM_ERROR).build();
        }
        return result;
    }
}
