package syncer.replica.rdb.datatype;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/7
 */
public class DB implements Serializable {

    private static final long serialVersionUID = 1L;

    private long dbNumber;
    /* rdb version 7 */
    private Long dbsize = null;
    /* rdb version 7 */
    private Long expires = null;

    public DB() {
    }

    public DB(long dbNumber) {
        this.dbNumber = dbNumber;
    }

    public DB(long dbNumber, long dbsize, long expires) {
        this.dbNumber = dbNumber;
        this.dbsize = dbsize;
        this.expires = expires;
    }

    public long getDbNumber() {
        return dbNumber;
    }

    public void setDbNumber(long dbNumber) {
        this.dbNumber = dbNumber;
    }

    public Long getDbsize() {
        return dbsize;
    }

    public void setDbsize(Long dbsize) {
        this.dbsize = dbsize;
    }

    public Long getExpires() {
        return expires;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        return "DB{" +
                "dbNumber=" + dbNumber +
                ", dbsize=" + dbsize +
                ", expires=" + expires +
                '}';
    }
}