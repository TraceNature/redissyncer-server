package syncer.syncerpluscommon.entity;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;
import syncer.syncerpluscommon.util.common.ServletUtils;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author 平行时空
 * @created 2018-09-18 21:04
 **/
@Component
public class ResultMap extends HashMap<String, Object>implements Serializable {
    public ResultMap() {
    }

    public ResultMap msg(String msg) {

        this.put("msg", msg);
        return this;
    }

    public ResultMap fail() {
        this.put("msg", "fail");
        return this;
    }

    public ResultMap success() {
        this.put("msg", "success");
        return this;
    }

    public ResultMap code(String code) {
        this.put("code", code);
        return this;
    }
    public ResultMap data(Object data) {
        this.put("data", data);
        return this;
    }


    public ResultMap add(String key,Object value) {
        this.put(key, value);
        return this;
    }


    public ResultMap status(int status) {
        ServletUtils.response().setStatus(status);
        return this;
    }

    public String json(){
        return JSON.toJSONString(this);
    }

    public  ResultMap start() {
        return new ResultMap();
    }

    public static   ResultMap builder() {
        return new ResultMap();
    }
    public static ResultMap getInstance() {
        return new ResultMap();
    }
}
