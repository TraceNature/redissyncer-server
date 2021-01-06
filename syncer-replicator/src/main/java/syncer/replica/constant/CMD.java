package syncer.replica.constant;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public class CMD {
    /**
     * PING 命令
     */
    public static final String PING = "PING";
    /**
     * PONG命令
     */
    public static final String PONG = "PONG";
    /**
     * 无权限
     */
    public static final String NOAUTH="NOAUTH";

    public static final String NOAUTH_MSG="-NOAUTH Authentication required.";

    /**
     * AUTH命令
     */
    public static final String AUTH="AUTH";

    /**
     * AUTH命令
     */
    public static final String OK="OK";

    /**
     * 无密码信息
     */
    public static final String NO_PASSWORD_MSG="no password";

    /**
     * 没有权限
     */
    public static final String NO_PERM="operation not permitted";

    public static final String REPLCONF="REPLCONF";

    public static final String LISTEN_PORT="listening-port";

    public static final String IP_ADRESS="ip-address";

    public static final String  CAPA="capa";

    public static final String ACK="ACK";

    public static final String GETACK="GETACK";

    public static final String EOF="eof";

    public static final String PSYNC2="psync2";

    public static final String SYNC="SYNC";
    public static final String PSYNC="PSYNC";

    public static final String CONTINUE="CONTINUE";

    public static final String FULL_RESYNC="FULLRESYNC";

    public static final String NOMASTERLINK="NOMASTERLINK";

    public static final String LOADING="LOADING";


    public static final String SELECT="SELECT";

}
