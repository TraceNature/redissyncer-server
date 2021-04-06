package syncer.replica.protocol;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import syncer.replica.cmd.CMD;
import syncer.replica.config.ReplicConfig;
import syncer.replica.exception.IncrementException;
import syncer.replica.exception.RedisAuthErrorException;
import syncer.replica.heartbeat.Heartbeat;
import syncer.replica.heartbeat.HeartbeatCommandRunner;
import syncer.replica.io.RedisInputStream;
import syncer.replica.io.RedisOutputStream;
import syncer.replica.datatype.command.BulkStringsReplyHandler;
import syncer.replica.parser.RedisSyncerRdbParser;
import syncer.replica.parser.protocol.ProtocolReplyParser;
import syncer.replica.replication.AbstractReplication;
import syncer.replica.util.strings.Strings;
import syncer.replica.util.type.CapaSyncType;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static syncer.replica.constant.Constants.DOLLAR;
import static syncer.replica.constant.Constants.STAR;

/**
 * 发送同步命令相关类
 *
 * @author: Eq Zhan
 * @create: 2021-03-15
 **/
@Slf4j
@Getter
@Setter
@AllArgsConstructor
@Builder
public class DefaultSyncRedisProtocol implements ISyncRedisProtocol {
    private ReplicConfig configuration;
    private ProtocolReplyParser replyParser;
    private RedisOutputStream outputStream;
    private Socket socket;
    private Heartbeat heartbeat;
    private AbstractReplication replication;

    /**
     * 发送PING
     * @throws IOException
     */
    @Override
    public void ping() throws IOException, IncrementException {
        log.info("[TASKID {}] send command [{}]",configuration.getTaskId(), CMD.PING);
        send(CMD.PING.getBytes());
        final String reply = Strings.toString(reply());
        log.info("[TASKID {}] {}",configuration.getTaskId(),reply);
        if (CMD.PONG.equalsIgnoreCase(reply)) {
            return;
        }
        if (reply.contains(CMD.NOAUTH)) {
            throw new AssertionError(reply);
        }
        if (reply.contains(CMD.NO_PERM)) {
            throw new AssertionError(CMD.NOAUTH_MSG);
        }
        log.warn("[TASKID {}][PING] failed. {}",configuration.getTaskId(), reply);
    }


    /**
     * 发送登陆命令
     * @param user
     * @param password
     * @throws IOException
     */
    @Override
    public void auth(String user, String password) throws IOException, RedisAuthErrorException, IncrementException {
        if (password != null) {
            // sha256 mask password
            String mask = "#" + Strings.mask(password);
            if (user != null) {
                log.info("[TASKID {}] AUTH {} {}",configuration.getTaskId(), user, mask);
                send(CMD.AUTH.getBytes(), user.getBytes(), password.getBytes());
            } else {
                log.info("[TASKID {}] AUTH {}",configuration.getTaskId(), mask);
                send(CMD.AUTH.getBytes(), password.getBytes());
            }
            final String reply = Strings.toString(reply());
            log.info("[TASKID {}] {}",configuration.getTaskId(),reply);
            if (CMD.OK.equals(reply)) {
                return;
            }
            if (reply.contains(CMD.NO_PASSWORD_MSG)) {
                log.warn("[TASKID {}][AUTH {} {}] failed. {}", configuration.getTaskId(),user, mask, reply);
                return;
            }
            throw new AssertionError("[TASKID "+configuration.getTaskId()+"][AUTH " + user + " " + mask + "] failed. " + reply);
        }
    }

    @Override
    public CapaSyncType fromSyncReply(String reply) throws IOException {
        return null;
    }



    /**
     * 发送端口信息
     *  REPLCONF listening-port <port-number>
     * @throws IOException
     */
    @Override
    public void sendSlaveListeningPort() throws IOException, IncrementException {
        // REPLCONF listening-prot ${port}
        log.info("[TASKID {}] REPLCONF listening-port {}", configuration.getTaskId(),socket.getLocalPort());
        send(CMD.REPLCONF.getBytes(), CMD.LISTEN_PORT.getBytes(), String.valueOf(socket.getLocalPort()).getBytes());
        final String reply = Strings.toString(reply());
        log.info("[TASKID {}] {}",configuration.getTaskId(),reply);
        if (CMD.OK.equals(reply)){
            return;
        }
        log.warn("[TASKID {}][REPLCONF listening-port {}] failed. {}", configuration.getTaskId(),socket.getLocalPort(), reply);
    }


    /**
     * 发送从IP信息
     * @throws IOException
     */
    @Override
    public void sendSlaveIpAddress() throws IOException, IncrementException {
        // REPLCONF ip-address ${address}
        log.info("[TASKID {}] REPLCONF ip-address {}", configuration.getTaskId(),socket.getLocalAddress().getHostAddress());
        send(CMD.REPLCONF.getBytes(), CMD.IP_ADRESS.getBytes(), socket.getLocalAddress().getHostAddress().getBytes());
        final String reply = Strings.toString(reply());
        log.info("[TASKID {}] {}",configuration.getTaskId(),reply);
        if (CMD.OK.equals(reply)) {
            return;
        }
        //redis 3.2+
        log.warn("[TASKID {}][REPLCONF ip-address {}] failed. {}", configuration.getTaskId(),socket.getLocalAddress().getHostAddress(), reply);
    }


    /**
     * REPLCONF capa  eof
     * @param cmd
     * @throws IOException
     */
    @Override
    public void sendCapa(String cmd) throws IOException, IncrementException {
        // REPLCONF capa eof
        log.info("[TASKID {}] REPLCONF capa {}", configuration.getTaskId(),cmd);
        send(CMD.REPLCONF.getBytes(), CMD.CAPA.getBytes(), cmd.getBytes());
        final String reply = Strings.toString(reply());
        log.info("[TASKID {}] {}",configuration.getTaskId(),reply);
        if (CMD.OK.equals(reply)){
            return;
        }
        log.warn("[TASKID {}][REPLCONF capa {}] failed. {}",configuration.getTaskId(), cmd, reply);
    }

    @Override
    public boolean SendSync(String replId, long offset) throws IOException {
        return false;
    }


    /**
     * 上报offset
     * 从节点在主线程中每隔1秒发送 replconf ack {offset}命令，给主节点上报自身当前的复制偏移量
     * 检查复制数据是否丢失，如果从节点数据丢失，再从主节点的复制缓冲区中拉取丢失数据
     */
    @Override
    public void heartbeat() {
        if(Objects.nonNull(heartbeat)){
            heartbeat.heartbeat(new HeartbeatCommandRunner() {
                @Override
                public void run() {
                    sendQuietly(CMD.REPLCONF.getBytes(), CMD.ACK.getBytes(), String.valueOf(configuration.getReplOffset()).getBytes());
                }
            },configuration.getHeartbeatPeriod(), configuration.getHeartbeatPeriod(), MILLISECONDS);
            log.info("[TASKID {}] heartbeat started.",configuration.getTaskId());
        }else {
            log.error("[TASKID {}] heartbeat start fail.",configuration.getTaskId());
        }
    }


    /**
     * 解析rdb
     * @param replication
     * @throws IOException
     * @throws IncrementException
     */
    public void parseDump(final AbstractReplication replication) throws IOException, RedisAuthErrorException, IncrementException {
        byte[] rawReply = reply(new BulkStringsReplyHandler() {
            @Override
            public byte[] handle(long len, RedisInputStream in) throws IOException, IncrementException {
                if (len != -1) {
                    log.info("[TASKID {}] RDB dump file size:{}",configuration.getTaskId(), len);
                } else {
                    log.info("[TASKID {}] Disk-less replication.",configuration.getTaskId());
                }
                if (len != -1 && configuration.isDiscardRdbEvent()) {
                    log.info("[TASKID {}] discard {} bytes", configuration.getTaskId(),len);
                    in.skip(len);
                } else {

                    new RedisSyncerRdbParser(in, replication).parse();
                    // skip 40 bytes delimiter when disk-less replication
                    if (len == -1) {
                        in.skip(40, false);
                    }
                }
                return CMD.OK.getBytes();
            }
        });
        String reply = Strings.toString(rawReply);
        if (CMD.OK.equals(reply)) {
            return;
        }
        throw new IOException("[TASKID "+configuration.getTaskId()+"] SYNC failed. reason : [" + reply + "]");
    }


    @SuppressWarnings("unchecked")
    public <T> T reply() throws IOException, IncrementException {
        return (T) replyParser.parse();
    }

    @Override
    public void setSocket(Socket socket) {
        this.socket=socket;
    }

    @Override
    public void setOutputStream(RedisOutputStream outputStream){
        this.outputStream=outputStream;
    }

    @Override
    public void setReplyParser(ProtocolReplyParser replyParser) {
        this.replyParser=replyParser;
    }


    public void sendQuietly(byte[] command, final byte[]... args) {
        try {
            send(command, args);
        } catch (IOException e) {
            // NOP
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T reply(BulkStringsReplyHandler handler) throws IOException, IncrementException {
        return (T) replyParser.parse(handler);
    }


    /**
     * 发送命令
     * @param command
     * @throws IOException
     */
    public void send(byte[] command) throws IOException {
        send(command, new byte[0][]);
    }

    /**
     * 发送命令
     * @param command
     * @param args
     * @throws IOException
     */
    public void send(byte[] command, final byte[]... args) throws IOException {
        outputStream.write(STAR);
        outputStream.write(String.valueOf(args.length + 1).getBytes());
        outputStream.writeCrLf();
        outputStream.write(DOLLAR);
        outputStream.write(String.valueOf(command.length).getBytes());
        outputStream.writeCrLf();
        outputStream.write(command);
        outputStream.writeCrLf();
        for (final byte[] arg : args) {
            outputStream.write(DOLLAR);
            outputStream.write(String.valueOf(arg.length).getBytes());
            outputStream.writeCrLf();
            outputStream.write(arg);
            outputStream.writeCrLf();
        }
        outputStream.flush();
    }
}
