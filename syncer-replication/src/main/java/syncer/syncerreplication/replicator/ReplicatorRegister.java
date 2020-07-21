package syncer.syncerreplication.replicator;

import syncer.syncerreplication.cmd.Command;
import syncer.syncerreplication.cmd.CommandName;
import syncer.syncerreplication.cmd.CommandParser;
import syncer.syncerreplication.constant.SyncerStatus;
import syncer.syncerreplication.entity.Configuration;
import syncer.syncerreplication.rdb.AbstractRdbVisitor;
import syncer.syncerreplication.rdb.datatype.Module;
import syncer.syncerreplication.rdb.module.ModuleParser;

/**
 * @author zhanenqiang
 * @Description Replicator注册器
 * @Date 2020/4/7
 */
public interface ReplicatorRegister {
    /*
     * Command
     */

    void builtInCommandParserRegister();

    CommandParser<? extends Command> getCommandParser(CommandName command);

    <T extends Command> void addCommandParser(CommandName command, CommandParser<T> parser);

    CommandParser<? extends Command> removeCommandParser(CommandName command);

    /*
     * Module
     */

    ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion);

    <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser);

    ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion);

    /*
     * Rdb
     */

    void setRdbVisitor(AbstractRdbVisitor rdbVisitor);

    AbstractRdbVisitor getRdbVisitor();

    boolean verbose();

    SyncerStatus getStatus();

    Configuration getConfiguration();
}
