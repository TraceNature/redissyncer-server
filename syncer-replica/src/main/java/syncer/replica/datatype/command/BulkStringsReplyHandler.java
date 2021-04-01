package syncer.replica.datatype.command;

import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;

import java.io.IOException;

/**
 * Bulk Strings 处理器
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public interface BulkStringsReplyHandler {
    byte[] handle(long len, RedisInputStream in) throws IOException, IncrementException;
}
