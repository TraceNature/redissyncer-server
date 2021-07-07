package syncer.replica.register;

import syncer.replica.config.ReplicConfig;
import syncer.replica.datatype.command.Command;
import syncer.replica.datatype.command.CommandName;
import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.ModuleParser;
import syncer.replica.datatype.rdb.module.Module;
import syncer.replica.parser.IRdbParser;
import syncer.replica.status.TaskStatus;

/**
 * replication注册器
 *
 * @author: Eq Zhan
 * @create: 2021-03-17
 **/
public interface ReplicationRegister {


    /**
     * Command
     */
    void builtInCommandParserRegister();

    CommandParser<? extends Command> getCommandParser(CommandName command);

    <T extends Command> void addCommandParser(CommandName command, CommandParser<T> parser);

    CommandParser<? extends Command> removeCommandParser(CommandName command);


    /**
     * Module
     *
     * @param moduleName
     * @param moduleVersion
     * @return
     */
    ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion);

    <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser);

    ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion);

    /**
     * RDB
     *
     * @param rdbParser
     */
    void setRdbParser(IRdbParser rdbParser);

    IRdbParser getRdbVisitor();


    TaskStatus getStatus();

    ReplicConfig getConfig();


    void closeClean();
}
