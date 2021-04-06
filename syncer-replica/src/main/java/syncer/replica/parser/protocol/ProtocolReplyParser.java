package syncer.replica.parser.protocol;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.datatype.command.BulkStringsReplyHandler;
import syncer.replica.exception.IncrementException;
import syncer.replica.handler.OffsetHandler;
import syncer.replica.io.RedisInputStream;
import syncer.replica.util.bytes.ByteBuilder;
import syncer.replica.util.code.RedisCodec;

import java.io.IOException;

import static syncer.replica.constant.Constants.COLON;
import static syncer.replica.constant.Constants.DOLLAR;
import static syncer.replica.constant.Constants.MINUS;
import static syncer.replica.constant.Constants.PLUS;
import static syncer.replica.constant.Constants.STAR;

/**
 * 协议应答解析器
 * resp_protocol.md
 * https://redis.io/topics/protocol/
 *
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
@Slf4j
public class ProtocolReplyParser implements IProtocolReplyParser {
    private final RedisInputStream in;
    private final RedisCodec codec;
    public ProtocolReplyParser(RedisInputStream in) {
        this(in, null);
    }

    public ProtocolReplyParser(RedisInputStream in, RedisCodec codec) {
        this.in = in;
        this.codec = codec;
    }

    public Object parse(BulkStringsReplyHandler handler, OffsetHandler offsetHandler) throws IOException, IncrementException {
        in.mark();
        Object rs = parse(handler);
        long len = in.unmark();
        if (offsetHandler != null){
            offsetHandler.handle(len);
        }
        return rs;
    }

    public Object parse() throws IOException, IncrementException {
        return parse(new DefaultBulkStringsReplyHandler(), null);
    }

    public Object parse(OffsetHandler offsetHandler) throws IOException, IncrementException {
        return parse(new DefaultBulkStringsReplyHandler(), offsetHandler);
    }




    /**
     * @param handler bulk reply handler
     * @return Object[] or byte[] or Long
     * @throws IOException when read timeout
     */
    @Override
    public Object parse(BulkStringsReplyHandler handler) throws IOException, IncrementException {
        while (true) {
            int first=in.read();
            switch (first) {
                case DOLLAR:
                    // RESP Bulk Stringsdabao
                    ByteBuilder builder = ByteBuilder.allocate(128);
                    while (true) {
                        while ((first = in.read()) != '\r') {
                            builder.put((byte) first);
                        }
                        if ((first = in.read()) == '\n') {
                            break;
                        } else {
                            builder.put((byte) first);
                        }
                    }
                    String payload = builder.toString();
                    long len = -1;
                    // disk-less replication
                    // $EOF:<40 bytes delimiter>
                    //无盘复制
                    if (!payload.startsWith("EOF:")) {
                        len = Long.parseLong(builder.toString());
                        // $-1\r\n. this is called null string.
                        // see http://redis.io/topics/protocol
                        if (len == -1){
                            return null;
                        }
                    } else {
                        if (handler instanceof DefaultBulkStringsReplyHandler) {
                            throw new AssertionError("Parse reply for disk-less replication can not use DefaultBlukStringsReplyHandler.");
                        }
                    }
                    if (handler != null){
                        return handler.handle(len, in);
                    }
                    throw new AssertionError("Callback is null");
                case COLON:
                    // RESP Integers
                    builder = ByteBuilder.allocate(128);
                    while (true) {
                        while ((first = in.read()) != '\r') {
                            builder.put((byte) first);
                        }
                        if ((first = in.read()) == '\n') {
                            break;
                        } else {
                            builder.put((byte) first);
                        }
                    }
                    // As integer
                    return Long.parseLong(builder.toString());
                case STAR:
                    // RESP Arrays
                    builder = ByteBuilder.allocate(128);
                    while (true) {
                        while ((first = in.read()) != '\r') {
                            builder.put((byte) first);
                        }
                        if ((first = in.read()) == '\n') {
                            break;
                        } else {
                            builder.put((byte) first);
                        }
                    }
                    len = Long.parseLong(builder.toString());
                    if (len == -1){
                        return null;
                    }
                    Object[] ary = new Object[(int) len];
                    for (int i = 0; i < len; i++) {
                        Object obj = parse(new DefaultBulkStringsReplyHandler());
                        ary[i] = obj;
                    }
                    return ary;
                case PLUS:
                    // RESP Simple Strings
                    builder = ByteBuilder.allocate(128);
                    while (true) {
                        while ((first = in.read()) != '\r') {
                            builder.put((byte) first);
                        }
                        if ((first = in.read()) == '\n') {
                            return builder.array();
                        } else {
                            builder.put((byte) first);
                        }
                    }
                case MINUS:
                    // RESP Errors
                    builder = ByteBuilder.allocate(128);
                    while (true) {
                        while ((first = in.read()) != '\r') {
                            builder.put((byte) first);
                        }
                        if ((first = in.read()) == '\n') {
                            return builder.array();
                        } else {
                            builder.put((byte) first);
                        }
                    }
                case '\n':
                    // skip +CONTINUE\r\n[\n]
                    // skip +FULLRESYNC 8de1787ba490483314a4d30f1c628bc5025eb761 2443808505[\n]$2443808505\r\nxxxxxxxxxxxxxxxx\r\n
                    // At this stage just a newline works as a PING in order to take the connection live
                    // bug fix
                    if (in.isMarked()) {
                        in.mark(Math.max(in.unmark() - 1, 0)); // skip [\n]
                    }
                    break;
                default:
                    log.error("expect [$,:,*,+,-] but: {}",(char) first);
                    throw new IncrementException("expect [$,:,*,+,-] but: " + (char) first);

            }
        }
    }

}
