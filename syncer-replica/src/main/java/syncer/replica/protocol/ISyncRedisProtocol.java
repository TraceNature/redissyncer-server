package syncer.replica.protocol;

import syncer.replica.exception.IncrementException;
import syncer.replica.exception.RedisAuthErrorException;
import syncer.replica.io.RedisOutputStream;
import syncer.replica.parser.protocol.ProtocolReplyParser;
import syncer.replica.replication.AbstractReplication;
import syncer.replica.replication.Replication;
import syncer.replica.util.type.CapaSyncType;

import java.io.IOException;
import java.net.Socket;

/**
 * @author: Eq Zhan
 * @create: 2021-03-15
 **/
public interface ISyncRedisProtocol {
    /**
     *  *3\r\n$3\r\nSET\r\n$4\r\ntest\r\n$5\r\n$value\r\n
     *  *[命令以及参数长度]\r\n$[command长度]\r\n[Command命令]\r\n$[长度]\r\n[key名字]\r\n$5\r\n[value]\r\n
     * @param command
     * @param args
     * @throws IOException
     */
    void send(byte[] command, final byte[]... args) throws IOException;

    void sendQuietly(byte[] command, final byte[]... args);

    /**
     * ping command
     * @return
     * @throws IOException
     */
    void ping() throws IOException, IncrementException;

    /**
     * 发送从节点端口信息 -> REPLCONF listening-port <port>
     *
     * @throws IOException
     */
    void sendSlaveListeningPort() throws IOException, IncrementException;


    /**
     * 发送从节点IP-> REPLCONF ip-address <IP>
     */
    void sendSlaveIpAddress() throws IOException, IncrementException;


    /**
     * 发送EOF能力（capability）-> REPLCONF capa eof
     * 是否支持EOF风格的RDB传输，用于无盘复制，就是能够解析出RDB文件的EOF流格式。用于无盘复制的方式中。
     * redis4.0支持两种能力 EOF 和 PSYNC2
     * redis4.0之前版本仅支持EOF能力
     */
    void sendCapa(String cmd) throws IOException, IncrementException;


    /**
     * 发送PSYNC->  PSYNC   {replid}  {offset}
     *
     * PSYNC {replid} {offset}
     *
     *         -->  FULLRESYNC  {replid}  {offset}   完整同步
     *         -->  CONTINUE 部分同步
     *         -->  -ERR 主服务器低于2.8,不支持psync,从服务器需要发送sync
     *         -->  NOMASTERLINK  重试
     *         -->  LOADING       重试
     *         -->  超过重试机制阈值宕掉任务
     */
    boolean SendSync(String replId,long offset) throws IOException;


    /**
     * 心跳检测
     */
    void heartbeat();

    /**
     * 登陆redis
     * @param user
     * @param password
     * @throws IOException
     * @throws RedisAuthErrorException
     */
    void auth(String user, String password) throws IOException, RedisAuthErrorException, IncrementException;


    /**
     * 解析rdb
     * @param replication
     * @throws IOException
     * @throws RedisAuthErrorException
     * @throws IncrementException
     */
    void parseDump(final AbstractReplication replication) throws IOException, RedisAuthErrorException, IncrementException;


    /**
     * 同步类型
     * @param reply
     * @return
     * @throws IOException
     */
    CapaSyncType fromSyncReply(String reply) throws IOException;

    <T> T reply() throws IOException, IncrementException;

    void setSocket(Socket socket);

    void setOutputStream(RedisOutputStream outputStream);


    void setReplyParser(ProtocolReplyParser replyParser);

    Replication getReplication();





}
