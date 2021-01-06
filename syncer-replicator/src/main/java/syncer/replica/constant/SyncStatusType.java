package syncer.replica.constant;

/**
 * @author zhanenqiang
 * @Description 同步类型【全量 增量】
 * @Date 2020/12/18
 */
public enum  SyncStatusType {
    /**
     * 存量同步状态
     */
    RdbSync(1,"存量同步状态"),
    /**
     * 增量同步状态
     */
    CommandSync(2,"增量同步状态");

    SyncStatusType(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;
}
