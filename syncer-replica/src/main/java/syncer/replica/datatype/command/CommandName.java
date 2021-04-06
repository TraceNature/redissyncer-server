package syncer.replica.datatype.command;

/**
 * @author: Eq Zhan
 * @create: 2021-03-17
 **/
public class CommandName {
    public final String name;

    private CommandName(String name) {
        this.name = name;
    }

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
