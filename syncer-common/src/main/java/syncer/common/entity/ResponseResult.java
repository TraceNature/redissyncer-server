package syncer.common.entity;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/7
 */
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ResponseResult<T> {
    /**
     * 状态码
     */
    private String code;
    /**
     * 状态信息
     */
    private String msg;
    /**
     * 返回数据对象
     */
    private T data;

    public String json(){
        return JSON.toJSONString(this);
    }
}
