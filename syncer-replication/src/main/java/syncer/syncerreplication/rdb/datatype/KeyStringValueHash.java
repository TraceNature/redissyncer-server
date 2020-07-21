package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description hash
 * @Date 2020/4/8
 */
public class KeyStringValueHash extends KeyValuePair<byte[], Map<byte[], byte[]>> implements Serializable {
    private static final long serialVersionUID = 1L;
}
