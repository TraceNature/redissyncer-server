package syncer.replica.parser.syncer.datatype;


import syncer.replica.kv.KeyValuePairEvent;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
public class DumpKeyValuePairEvent extends KeyValuePairEvent<byte[], byte[]> implements Serializable {
    private static final long serialVersionUID = 1L;
}
