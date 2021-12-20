package syncer.replica.datatype.command.set;

import java.io.Serializable;

public enum  XATType implements Serializable {

    /**
     * not set
     */
    NONE,

    /**
     * timestamp-seconds
     */
    EXAT,

    /**
     * timestamp-milliseconds
     */
    PXAT;


}
