package syncer.transmission.constants;

/**
 * 命令/key过滤器类型
 * @author: Eq Zhan
 * @create: 2021-02-01
 **/
public enum CommandKeyFilterType {
    NONE,
    /**
     * 只接受指定的命令
     * command大小写不敏感
     */
    COMMAND_FILTER_ACCEPT,
    /**
     * 只接受指定key
     * key大小写敏感
     */
    KEY_FILTER_ACCEPT,

    /**
     * 同时生效 && 两者都满足放行
     */
    COMMAND_AND_KEY_FILTER_ACCEPT,

    /**
     * 指定的command 和key都接受
     * 两者满足任意一者即生效放行
     */
    COMMAND_OR_KEY_FILTER_ACCEPT,
    /**
     * 只拒绝指定的command
     */
    COMMAND_FILTER_REFUSE,
    /**
     * 只拒绝指定的key
     */
    KEY_FILTER_REFUSE,
    /**
     * 拒绝指定的key和command
     * 两者满足任意一者即生效拒绝
     */
    COMMAND_OR_KEY_FILTER_REFUSE,

    /**
     * 同时生效 &&  两者都满足才拒绝
     */
    COMMAND_AND_KEY_FILTER_REFUSE


}
