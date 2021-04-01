package syncer.replica.datatype.command;

/**
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public interface CommandParser<T extends Command> {
    T parse(Object[] command);
}