package syncer.syncerreplication.rdb.datatype;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description List
 * @Date 2020/4/8
 */
public class KeyStringValueList extends KeyValuePair<byte[], List<byte[]>>implements Serializable {
    private static final long serialVersionUID = 1L;
}
