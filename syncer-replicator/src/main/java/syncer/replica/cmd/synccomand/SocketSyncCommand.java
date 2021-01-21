// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.
package syncer.replica.cmd.synccomand;

import syncer.replica.cmd.BulkReplyHandler;
import syncer.replica.cmd.ReplyParser;
import syncer.replica.constant.CMD;
import syncer.replica.entity.Configuration;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;
import syncer.replica.io.RedisOutputStream;
import syncer.replica.rdb.RedisRdbParser;
import syncer.replica.replication.AbstractReplication;
import syncer.replica.util.XScheduledExecutorService;
import syncer.replica.util.objectutil.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static syncer.replica.constant.CMD.REPLCONF;
import static syncer.replica.constant.Constants.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author zhanenqiang
 * @Description 发送同步命令相关类
 * @Date 2020/8/7
 */
@Slf4j
@Getter
@Setter
@AllArgsConstructor
@Builder
public class SocketSyncCommand {
    private Configuration configuration;
    private ReplyParser replyParser;
    private RedisOutputStream outputStream;
    private Socket socket;
    private ScheduledFuture<?> heartbeat;
    private XScheduledExecutorService executor;
    private AbstractReplication replication;

    /**
     * 发送PING
     * @throws IOException
     */
    public void sendPing() throws IOException, IncrementException {
        log.info("[TASKID {}] {}",configuration.getTaskId(),CMD.PING);
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
    public void auth(String user, String password) throws IOException, IncrementException {
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


    /**
     * 发送端口信息
     *  REPLCONF listening-port <port-number>
     * @throws IOException
     */
    public void sendSlavePort() throws IOException, IncrementException {
        // REPLCONF listening-prot ${port}
        log.info("[TASKID {}] REPLCONF listening-port {}", configuration.getTaskId(),socket.getLocalPort());
        send(REPLCONF.getBytes(), CMD.LISTEN_PORT.getBytes(), String.valueOf(socket.getLocalPort()).getBytes());
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
    public void sendSlaveIp() throws IOException, IncrementException {
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
    public void sendSlaveCapa(String cmd) throws IOException, IncrementException {
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


    /**
     * 上报offset
     * 从节点在主线程中每隔1秒发送 replconf ack{offset}命令，给主节点上报自身当前的复制偏移量
     * 检查复制数据是否丢失，如果从节点数据丢失，再从主节点的复制缓冲区中拉取丢失数据
     */
    public void heartbeat() {
        assert heartbeat == null || heartbeat.isCancelled();
        heartbeat = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                sendQuietly(REPLCONF.getBytes(), CMD.ACK.getBytes(), String.valueOf(configuration.getReplOffset()).getBytes());
            }
        }, configuration.getHeartbeatPeriod(), configuration.getHeartbeatPeriod(), MILLISECONDS);
        log.info("[TASKID {}] heartbeat started.",configuration.getTaskId());
    }





    public void parseDump(final AbstractReplication replicator) throws IOException, IncrementException {
        byte[] rawReply = reply(new BulkReplyHandler() {
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
                    new RedisRdbParser(in, replicator).parse();
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


    public void sendQuietly(byte[] command, final byte[]... args) {
        try {
            send(command, args);
        } catch (IOException e) {
            // NOP
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T reply(BulkReplyHandler handler) throws IOException, IncrementException {
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
