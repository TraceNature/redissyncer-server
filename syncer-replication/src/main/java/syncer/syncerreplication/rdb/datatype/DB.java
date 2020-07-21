package syncer.syncerreplication.rdb.datatype;

import lombok.*;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/7
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class DB implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * dbNumber
     */
    private long dbNumber;
    /* rdb version 7 */
    /**
     * dbsize
     */
    private Long dbsize = null;
    /* rdb version 7 */
    /**
     *
     */
    private Long expires = null;



    public DB(long dbNumber) {
        this.dbNumber = dbNumber;
    }


}
