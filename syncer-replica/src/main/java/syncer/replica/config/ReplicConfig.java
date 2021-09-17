package syncer.replica.config;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import syncer.replica.socket.SslContextFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务信息配置
 * @author: Eq Zhan
 * @create: 2021-03-12
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplicConfig {
    private String taskId;

    /**
     *Socket 连接超时时间
     */
    private int connectionTimeout = 60000;

    private int readTimeout=60000;

    /**
     * socket receive buffer size
     */
    private int receiveBufferSize = 0;

    /**
     * socket send buffer size
     */
    private int sendBufferSize = 0;


    /**
     * connection retry times. if retries <= 0 then always retry
     */
    private int retries = 3;


    /**
     * retry time interval
     */
    private int retryTimeInterval = 1000;

    /**
     * redis input stream buffer size
     */
    private int bufferSize = 8 * 1024;


    /**
     * auth user (redis 6.0)
     */
    private String authUser = null;

    /**
     * auth password
     */
    private String authPassword = null;

    /**
     * sentinel auth password
     */
    private String sentinelAuthPassword = null;

    private String masterRedisName=null;


    /**
     * discard rdb event
     */
    private boolean discardRdbEvent = false;

    /**
     * async buffer size
     */
    private int asyncCachedBytes = 512 * 1024;



    /**
     * psync master repl_id
     */
    private String replId = "?";

    /**
     * psync2 repl_stream_db
     */
    private int replStreamDB = -1;

    /**
     * psync offset
     */
    private final AtomicLong replOffset = new AtomicLong(-1);


    /**
     * used in psync heartbeat
     */
    private int heartbeatPeriod = 1000;

    private ScheduledExecutorService scheduledExecutor;



    /**
     * open ssl connection
     */
    private boolean ssl = false;


    /**
     * ssl socket factory
     */
    private SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    /**
     * ssl context factory
     *
     * @since 3.4.0
     */
    private SslContextFactory sslContextFactory;

    /**
     * ssl parameters
     */
    private SSLParameters sslParameters;


    /**
     * hostname verifier
     */
    private HostnameVerifier hostnameVerifier;


    /**
     * 用于进度计算
     */
    private long readFileSize;
    /**
     * 文件总大小
     */
    private long fileSize;

    /**
     * 当全量同步目标存在相同的key时，用于需是否覆盖目标中的key
     * 用于全量阶段非 dump restore类型的命令
     * 若设为true 则
     * 全量+增量同步时，当全量同步后，由于源redis写入数据速度过大，导致全量完成后所记录的offset再源redis已经被刷过，导致同步失败
     * --> true      当全量完成--->增量时断开--->重连  psync replid  offset --> FULLRESYNC（重新全量） -->   若设置为true 则接受重新全量
     *                                                                 --> CONTINUE(可继续增量)
     *
     * --> false     若设置为false 则遇到FULLRESYNC则宕掉任务
     *
     * 何时适合false 何时适合 true？？
     * 根据redis协议默认是true ，因为默认全量数据走dump restore时是个幂等操作，可以覆盖目标内的重复数据，此时可以重复全量
     * 为false，适合本同步工具，因为默认的同步过程中涉及到大key拆分，所以全量过程中list等命令会变成非幂等，重复写入会造成数据重复，因此遇到FULLRESYNC推荐broken，人工干预
     */
    private boolean fullResyncBrokenTask=false;

    public void setReplOffset(long offset){
        replOffset.set(offset);
    }


    public long getReplOffset() {
        return replOffset.get();
    }


    public ReplicConfig addOffset(long offset) {
        this.replOffset.addAndGet(offset);
        return this;
    }
    public static ReplicConfig defaultConfig(){
        ReplicConfig defaultConfig=new ReplicConfig();
        defaultConfig.setRetries(3);
        return defaultConfig;
    }


    public static ReplicConfig valueOf(RedisURI uri) {
        ReplicConfig configuration = defaultConfig();
        Map<String, String> parameters = uri.parameters;
        if (parameters.containsKey("connectionTimeout")) {
            configuration.setConnectionTimeout(getInt(parameters.get("connectionTimeout"), 60000));
        }
        if (parameters.containsKey("readTimeout")) {
            configuration.setReadTimeout(getInt(parameters.get("readTimeout"), 60000));
        }
        if (parameters.containsKey("receiveBufferSize")) {
            configuration.setReceiveBufferSize(getInt(parameters.get("receiveBufferSize"), 0));
        }
        if (parameters.containsKey("sendBufferSize")) {
            configuration.setSendBufferSize(getInt(parameters.get("sendBufferSize"), 0));
        }
        if (parameters.containsKey("retries")) {
            configuration.setRetries(getInt(parameters.get("retries"), 5));
        }
        if (parameters.containsKey("retryTimeInterval")) {
            configuration.setRetryTimeInterval(getInt(parameters.get("retryTimeInterval"), 1000));
        }
        if (parameters.containsKey("bufferSize")) {
            configuration.setBufferSize(getInt(parameters.get("bufferSize"), 8 * 1024));
        }
        if (parameters.containsKey("authUser")) {
            configuration.setAuthUser(parameters.get("authUser"));
        }
        if (parameters.containsKey("authPassword")) {
            configuration.setAuthPassword(parameters.get("authPassword"));
        }

        if (parameters.containsKey("masterRedisName")) {
            configuration.setMasterRedisName(parameters.get("masterRedisName"));
        }

        if (parameters.containsKey("sentinelAuthPassword")) {
            configuration.setSentinelAuthPassword(parameters.get("sentinelAuthPassword"));
        }

        if (parameters.containsKey("discardRdbEvent")) {
            configuration.setDiscardRdbEvent(getBool(parameters.get("discardRdbEvent"), false));
        }

        if (parameters.containsKey("fullResyncBrokenTask")) {
            configuration.setDiscardRdbEvent(getBool(parameters.get("fullResyncBrokenTask"), false));
        }
        if (parameters.containsKey("asyncCachedBytes")) {
            configuration.setAsyncCachedBytes(getInt(parameters.get("asyncCachedBytes"), 512 * 1024));
        }
//        if (parameters.containsKey("rateLimit")) {
//            configuration.setRateLimit(getInt(parameters.get("rateLimit"), 0));
//        }

        if (parameters.containsKey("heartbeatPeriod")) {
            configuration.setHeartbeatPeriod(getInt(parameters.get("heartbeatPeriod"), 1000));
        }

        if (parameters.containsKey("ssl")) {
            configuration.setSsl(getBool(parameters.get("ssl"), false));
        }
        if (parameters.containsKey("replId")) {
            configuration.setReplId(parameters.get("replId"));
        }
        if (parameters.containsKey("replStreamDB")) {
            configuration.setReplStreamDB(getInt(parameters.get("replStreamDB"), -1));
        }
        if (parameters.containsKey("replOffset")) {
            configuration.setReplOffset(getLong(parameters.get("replOffset"), -1L));
        }
        // redis 6
        if (uri.isSsl()) {
            configuration.setSsl(true);
        }
        if (uri.getUser() != null) {
            configuration.setAuthUser(uri.getUser());
        }
        if (uri.getPassword() != null) {
            configuration.setAuthPassword(uri.getPassword());
        }

        return configuration;
    }


    public ReplicConfig merge(SslReplicConfig sslConfiguration) {
        if (sslConfiguration == null) {
            return this;
        }
        this.setSslParameters(sslConfiguration.getSslParameters());
        this.setSslSocketFactory(sslConfiguration.getSslSocketFactory());
        this.setHostnameVerifier(sslConfiguration.getHostnameVerifier());
        this.setSslContextFactory(sslConfiguration.getSslContextFactory());
        return this;
    }

    private static boolean getBool(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if ("false".equals(value) || "no".equals(value)) {
            return false;
        }
        if ("true".equals(value) || "yes".equals(value)) {
            return true;
        }
        return defaultValue;
    }

    private static int getInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long getLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean isFullResyncBrokenTask() {
        return fullResyncBrokenTask;
    }

    public void setFullResyncBrokenTask(boolean fullResyncBrokenTask) {
        this.fullResyncBrokenTask = fullResyncBrokenTask;
    }

    public ReplicConfig setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }
}
