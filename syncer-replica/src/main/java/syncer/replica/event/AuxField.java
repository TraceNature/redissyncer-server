package syncer.replica.event;

import syncer.replica.kv.AbstractEvent;

/**
 * @author: Eq Zhan
 * @create: 2021-03-17
 **/
public class AuxField extends AbstractEvent {

    private static final long serialVersionUID = 1L;

    private String auxKey;
    private String auxValue;

    public AuxField() {
    }

    public AuxField(String auxKey, String auxValue) {
        this.auxKey = auxKey;
        this.auxValue = auxValue;
    }

    public String getAuxKey() {
        return auxKey;
    }

    public String getAuxValue() {
        return auxValue;
    }

    public void setAuxKey(String auxKey) {
        this.auxKey = auxKey;
    }

    public void setAuxValue(String auxValue) {
        this.auxValue = auxValue;
    }

    @Override
    public String toString() {
        return "AuxField{" +
                "auxKey='" + auxKey + '\'' +
                ", auxValue='" + auxValue + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
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
