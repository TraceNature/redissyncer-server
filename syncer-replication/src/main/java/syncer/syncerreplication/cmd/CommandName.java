package syncer.syncerreplication.cmd;

import lombok.AllArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 命令名称
 * @Date 2020/4/7
 */
@AllArgsConstructor
public class CommandName {
    public final String name;


    public static CommandName name(String key) {
        return new CommandName(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CommandName that = (CommandName) o;
        return name.toUpperCase().equals(that.name.toUpperCase());
    }


    @Override
    public int hashCode() {
        return name.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return "CommandName{" +
                "name='" + name + '\'' +
                '}';
    }
}
