package syncer.transmission.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import syncer.transmission.constants.CommandKeyFilterType;

/**
 * @author: Eq Zhan
 * @create: 2021-02-19
 **/
@AllArgsConstructor
@Getter
@Setter
@Builder
public class KeyFilterDto {
    /**
     * 命令过滤器
     */

    private String commandFilter;

    /**
     * Key过滤器
     */
    private String keyFilter;

    /**
     * 过滤类型
     */
    private CommandKeyFilterType filterType;


}
