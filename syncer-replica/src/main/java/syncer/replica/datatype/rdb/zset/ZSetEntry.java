package syncer.replica.datatype.rdb.zset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import syncer.replica.util.strings.Strings;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * ZSET 结构
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class ZSetEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] element;
    private double score;


    @Override
    public String toString() {
        return "[" + Strings.toString(element) + ", " + score + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZSetEntry zSetEntity = (ZSetEntry) o;
        return Double.compare(zSetEntity.score, score) == 0 &&
                Arrays.equals(element, zSetEntity.element);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(score);
        result = 31 * result + Arrays.hashCode(element);
        return result;
    }
}
