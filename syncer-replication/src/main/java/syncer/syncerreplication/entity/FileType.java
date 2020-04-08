package syncer.syncerreplication.entity;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/8
 */
public enum FileType {
    //AOF REB MIXED 分别对应 aof rdb mixed混合文件
    AOF, RDB, MIXED,ONLINEAOF,ONLINERDB,SYNC,ONLINEMIXED,COMMANDDUMPUP;

    /**
     * @param type string type
     * @return FileType
     * @since 2.4.0
     */
    static FileType parse(String type) {
        if (type == null) {
            return null;
        } else if ("aof".equalsIgnoreCase(type)) {
            return AOF;
        } else if ("rdb".equalsIgnoreCase(type)) {
            return RDB;
        } else if ("mix".equalsIgnoreCase(type)) {
            return MIXED;
        } else if ("mixed".equalsIgnoreCase(type)) {
            return MIXED;
        } else if ("onelinerdb".equalsIgnoreCase(type)) {
            return ONLINERDB;
        } else if ("onelineaof".equalsIgnoreCase(type)) {
            return ONLINEAOF;
        }else if("commanddumpup".equalsIgnoreCase(type)){
            return COMMANDDUMPUP;
        } else {
            return null;
        }
    }
}
