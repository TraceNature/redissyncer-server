package syncer.replica.datatype.command.string;

import java.io.Serializable;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public enum Op implements Serializable {
    AND, OR, XOR, NOT
}
