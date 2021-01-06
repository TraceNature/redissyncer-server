package syncer.replica.cmd;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/10
 */
public interface CommandParser<T extends Command> {
    T parse(Object[] command);
}

