package syncer.transmission.entity;

import lombok.*;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffSetEntity implements Serializable {
    private static final long serialVersionUID = -5809782578272943997L;
    private String  replId;
    private final AtomicLong replOffset = new AtomicLong(-1);
}
