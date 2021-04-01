package syncer.replica.entity;

import lombok.*;

import java.io.Serializable;

/**
 * @author: Eq Zhan
 * @create: 2021-03-15
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RedisDB implements Serializable {
    private static final long serialVersionUID = 1L;
    private long currentDbNumber;
    private Long dbSize;
    private Long expires;


    public RedisDB(long currentDbNumber) {
        this.currentDbNumber = currentDbNumber;
    }
}
