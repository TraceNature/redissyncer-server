package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description SET
 * @Date 2020/4/8
 */
public class KeyStringValueSet extends KeyValuePair<byte[], Set<byte[]>>implements Serializable {
    private static final long serialVersionUID = 1L;
}