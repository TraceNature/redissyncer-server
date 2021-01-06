package syncer.transmission.constants;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/10
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComanndResponseType {
    /**
     *  1 OK
     *  2 LONG >=0
     *  3 LONG >=0 & <0
     *  4 String
     *  5 Double
     *  6 ArrayList
     *  7 HashSet
     *  8 PONG
     *  9 Long Double
     */
    int type;
    String command;
    String commandResponse;
}
