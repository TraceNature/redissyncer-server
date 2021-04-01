package syncer.replica.parser.protocol;

import syncer.replica.datatype.command.BulkStringsReplyHandler;
import syncer.replica.exception.IncrementException;
import syncer.replica.handler.OffsetHandler;
import java.io.IOException;

/**
 * @author: Eq Zhan
 * @create: 2021-03-19
 **/
public interface IProtocolReplyParser {
    Object parse() throws IOException, IncrementException;
    Object parse(BulkStringsReplyHandler handler, OffsetHandler offsetHandler) throws IOException, IncrementException;
    Object parse(BulkStringsReplyHandler handler) throws IOException, IncrementException;
}
