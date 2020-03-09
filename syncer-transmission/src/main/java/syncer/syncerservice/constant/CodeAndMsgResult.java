package syncer.syncerservice.constant;

import lombok.ToString;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/9
 */
@ToString
public enum CodeAndMsgResult {

    SUCCESS(2000,"SUCCESS");

    private  Integer code;
    private String msg;

    CodeAndMsgResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }



}
