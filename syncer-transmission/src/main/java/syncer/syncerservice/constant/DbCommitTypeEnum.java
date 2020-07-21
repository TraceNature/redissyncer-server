package syncer.syncerservice.constant;

import lombok.AllArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 数据库信息提交枚举
 * @Date 2020/7/6
 */
@AllArgsConstructor
public enum DbCommitTypeEnum {
    /**
     * 抛弃命令
     * code 1 插入信息
     */
    AbandonCommand(1,"抛弃命令信息插入");
    private  Integer code;
    private String msg;


}
