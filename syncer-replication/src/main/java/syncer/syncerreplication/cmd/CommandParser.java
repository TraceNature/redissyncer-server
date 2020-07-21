package syncer.syncerreplication.cmd;

/**
 * @author zhanenqiang
 * @Description 命令解析器接口
 * @Date 2020/4/7
 */
public interface CommandParser<T extends Command> {
    T parse(Object[] command);
}
