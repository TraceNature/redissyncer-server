package syncer.replica.datatype.command.string;

import java.io.Serializable;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public enum OverFlowType implements Serializable {
    WRAP, SAT, FAIL
}