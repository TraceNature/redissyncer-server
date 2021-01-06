package syncer.replica.cmd.jimdb;

import syncer.replica.cmd.impl.AbstractCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


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
