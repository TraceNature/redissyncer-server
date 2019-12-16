package syncer.syncerservice.util;

import io.lettuce.core.protocol.CommandType;

public class EnumUtils {
    public static CommandType getSexEnumByCode(String cmd){
        return  CommandType.valueOf(cmd.trim().toUpperCase());
//        for(CommandType commandTypeEnum : CommandType.values()){
//
//            if(CommandType.valueOf(cmd.trim().toUpperCase()).equals(commandTypeEnum)){
//                return commandTypeEnum;
//            }
//        }
//        return null;
    }


    public EnumUtils() {
    }


    public static CommandType getSexEnumByCode(byte[] cmd) {
        String cmds = new String(cmd);
        return CommandType.valueOf(cmds.trim().toUpperCase());
    }


}
