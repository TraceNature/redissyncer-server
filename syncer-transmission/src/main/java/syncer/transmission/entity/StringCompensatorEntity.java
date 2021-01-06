package syncer.transmission.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * String append 补偿
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class StringCompensatorEntity {
    private Long dbNum;
    private byte[]key;
    private String stringKey;
    private StringBuilder value;
    private long ms;

}
