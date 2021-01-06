package syncer.replica.register;

import syncer.replica.cmd.Command;
import syncer.replica.cmd.CommandName;
import syncer.replica.cmd.CommandParser;
import syncer.replica.entity.Configuration;
import syncer.replica.entity.Status;
import syncer.replica.rdb.RedisRdbVisitor;
import syncer.replica.rdb.datatype.Module;
import syncer.replica.rdb.module.ModuleParser;


/**
 * @author zhanenqiang
 * @Description replication注册器
 * @Date 2020/8/7
 */

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
     * @param moduleName
     * @param moduleVersion
     * @return
     */
    ModuleParser<? extends Module> getModuleParser(String moduleName, int moduleVersion);

    <T extends Module> void addModuleParser(String moduleName, int moduleVersion, ModuleParser<T> parser);

    ModuleParser<? extends Module> removeModuleParser(String moduleName, int moduleVersion);

    /**
     * RDB
     * @param rdbVisitor
     */
    void setRdbVisitor(RedisRdbVisitor rdbVisitor);

    RedisRdbVisitor getRdbVisitor();

    boolean verbose();

    Status getStatus();

    Configuration getConfiguration();

}
