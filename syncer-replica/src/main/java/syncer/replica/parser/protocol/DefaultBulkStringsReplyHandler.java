package syncer.replica.parser.protocol;

import syncer.replica.datatype.command.BulkStringsReplyHandler;
import syncer.replica.exception.IncrementException;
import syncer.replica.io.RedisInputStream;

import java.io.IOException;

/**
 *  resp_protocol.md   RESP Bulk Strings
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public class DefaultBulkStringsReplyHandler implements BulkStringsReplyHandler {
    @Override
    public byte[] handle(long len, RedisInputStream in) throws IOException, IncrementException {
        byte[]result;
        if(len==0){
            result=new byte[]{};
        }else {
            result=in.readBytes(len).first();
        }
        int crlf=in.read();
        if(crlf!='\r'){
            throw new IncrementException("expect '\\r' but :" + (char) crlf);
        }
        crlf=in.read();
        if(crlf!='\n'){
            throw new IncrementException("expect '\\n' but :" + (char) crlf);
        }

        return result;
    }
}
