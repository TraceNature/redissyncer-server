package syncer.syncerpluscommon.entity;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/10/28
 */

public class ResponseResult<T> {
    private String code;
    private String msg;
    private T data;
}
