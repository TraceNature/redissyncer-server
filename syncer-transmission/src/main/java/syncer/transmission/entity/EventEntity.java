package syncer.transmission.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.rdb.datatype.ZSetEntry;
import syncer.transmission.compensator.PipeLineCompensatorEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class EventEntity implements Serializable {
    private byte[]key;
    private byte[][]valueList;
    private byte[]cmd;
    private List<byte[]> lpush_value;
    private Set<byte[]> members;
    private  Set<ZSetEntry> zaddValue;
    private Map<byte[], byte[]> hash_value;
    private byte[]value;
    private long ms=-1;
    private String stringKey;
    private Long dbNum;
    private PipeLineCompensatorEnum pipeLineCompensatorEnum;
    private boolean highVersion;
    public String getStringKey() {
        return stringKey;
    }
}
