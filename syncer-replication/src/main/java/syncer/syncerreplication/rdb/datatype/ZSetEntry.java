package syncer.syncerreplication.rdb.datatype;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import syncer.syncerreplication.util.objectUtils.Strings;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/8
 */
@NoArgsConstructor
@Getter
@Setter
public class ZSetEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private byte[] element;
    private double score;


    public ZSetEntry(byte[] element, double score) {
        this.element = element;
        this.score = score;
    }



    @Override
    public String toString() {
        return "[" + Strings.toString(element) + ", " + score + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZSetEntry zSetEntry = (ZSetEntry) o;
        return Double.compare(zSetEntry.score, score) == 0 &&
                Arrays.equals(element, zSetEntry.element);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(score);
        result = 31 * result + Arrays.hashCode(element);
        return result;
    }
}
