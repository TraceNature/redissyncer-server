package syncer.transmission.client.cluster;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedisClusterSlot {
    private long startSlot;
    private long endSlot;
    private String host;
    private long port;
    private String replId;
}
