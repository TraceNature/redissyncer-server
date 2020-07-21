package syncer.syncerreplication.rdb.datatype;

import lombok.*;
import syncer.syncerreplication.event.AbstractEvent;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description Aux字段
 * @Date 2020/4/8
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AuxField extends AbstractEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String auxKey;
    private String auxValue;





    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuxField auxField = (AuxField) o;

        return auxKey.equals(auxField.auxKey);
    }

    @Override
    public int hashCode() {
        return auxKey.hashCode();
    }
}
