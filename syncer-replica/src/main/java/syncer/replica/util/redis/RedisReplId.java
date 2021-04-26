package syncer.replica.util.redis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import syncer.common.util.RegexUtil;
import syncer.jedis.Jedis;

import java.net.URISyntaxException;
@Slf4j
public class RedisReplId {
    public final static String FIRST_RGEX = "repl_backlog_first_byte_offset:(.*?)\r\n";
    public final static String END_RGEX = "master_repl_offset:(.*?)\r\n";
    public final static String REPLID="master_replid:(.*?)\r\n";
    /**
     * jimDb_shard_id
     */
    public final static String JIMDB_SHARD_ID="shard_id:(.*?)\r\n";

    public final static String RUNID_RGEX= "run_id:(.*?)\r\n";

    public static final String END_BUF="endbuf";

    /**
     * 获取Redis Buffer值
     * @param
     * @param type
     * @return
     * @throws URISyntaxException
     */
    public  String[] selectSyncerBuffer(String host,Integer port,String authUser,String password,String type) throws URISyntaxException {
        /**
         * 源目标
         */
        Jedis target = null;
        String[] version = new String[2];
        version[0]="0";
        try{
            target = new Jedis(host,port);
            if(!StringUtils.isEmpty(authUser)&&!StringUtils.isEmpty(password)){
                Object targetAuth = target.auth(authUser+" "+password);
            }else if (!StringUtils.isEmpty(password)) {
                Object targetAuth = target.auth(password);
            }
            String info=target.info();
            version = getRedisBuffer(info,type);
        }catch (Exception e){
            e.printStackTrace();
            log.error("check redis replid error  {} : {} {}",host, port,e.getMessage());
        } finally {
            if (target != null) {
                target.close();
            }
        }
        return version;
    }

    private  String[] getRedisBuffer(String info,String type) {
        String[] version = new String[2];
        version[0]="0";
        try{

            if(END_BUF.equalsIgnoreCase(type.trim())){
                if(RegexUtil.getSubUtilSimple(info, END_RGEX).length()>0){
                    version[0] = RegexUtil.getSubUtilSimple(info, END_RGEX);
                }


            }else {

                if(RegexUtil.getSubUtilSimple(info, END_RGEX).length()>0){
                    version[0] = RegexUtil.getSubUtilSimple(info, END_RGEX);
                }
                /**
                 //从头部开始
                 if(RegexUtil.getSubUtilSimple(info, FIRST_RGEX).length()>0){
                 version[0] = RegexUtil.getSubUtilSimple(info, FIRST_RGEX);
                 }
                 **/
            }
            String runid1=RegexUtil.getSubUtilSimple(info, REPLID);

            if(!StringUtils.isEmpty(runid1.trim())){
                version[1]=runid1;
            }else {
                if(RegexUtil.getSubUtilSimple(info, JIMDB_SHARD_ID).length()>0){
                    version[1]=RegexUtil.getSubUtilSimple(info, JIMDB_SHARD_ID);
                }else {
                    version[1]=RegexUtil.getSubUtilSimple(info, RUNID_RGEX);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return version;

    }
}
