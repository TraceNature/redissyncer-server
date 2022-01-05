package syncer.transmission.tikv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@ToString
public class TikvKey {
    private String instId;
    private Long currentDbNumber;
    private String stringKey;
    private byte[] key;
    private TikvKeyType keyType;
    private Integer index;
    private String setValue;
}
