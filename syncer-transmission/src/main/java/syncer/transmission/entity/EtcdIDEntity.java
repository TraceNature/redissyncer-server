package syncer.transmission.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Eq Zhan
 * @create: 2021-03-09
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EtcdIDEntity {
    @Builder.Default
    private AtomicInteger id=new AtomicInteger(0);
}
