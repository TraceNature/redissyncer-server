package syncer.syncerplusredis.cmd.jimdb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import syncer.syncerplusredis.cmd.impl.AbstractCommand;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/29
 */
@AllArgsConstructor
public class JimdbFirstCommand extends AbstractCommand {
    @Getter @Setter
    private Object[] command;

}
