package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;
import java.util.Set;

/**
 * @author zhanenqiang
 * @Description ZSet
 * @Date 2020/4/8
 */
public class KeyStringValueZSet extends KeyValuePair<byte[], Set<ZSetEntry>> implements Serializable {
    private static final long serialVersionUID = 1L;
}

