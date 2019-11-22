package syncer.syncerplusservice.task;

import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusservice.pool.ConnectionPool;
import syncer.syncerplusservice.pool.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.concurrent.Callable;

@Slf4j
public class CommitSendTask implements Callable<Object> {

    private DefaultCommand command;
    private RedisClient redisClient;
    private ConnectionPool pool;
    private StringBuffer info;
    private String dbIndex;



    public CommitSendTask(DefaultCommand command, RedisClient redisClient, ConnectionPool pool, StringBuffer info, String dbIndex) {
        this.command = command;
        this.redisClient = redisClient;
        this.pool = pool;
        this.info = info;
        this.dbIndex=dbIndex;
    }

    /**
     * 缺少校验
     * @return
     * @throws Exception
     */
    @Override
    public Object call() throws Exception {
        Object r=null;


//        int i=3;
//        while (i>0){
            if(!StringUtils.isEmpty(dbIndex)){

                Object ir = redisClient.send("SELECT".getBytes(), dbIndex.getBytes());

                if(ir.equals("OK")) {
                    r = redisClient.send(command.getCommand(), command.getArgs());

                }else {
                    ir = redisClient.send("SELECT".getBytes(), dbIndex.getBytes());
                    r= redisClient.send(command.getCommand(), command.getArgs());

                }
            }else {
                r = redisClient.send(command.getCommand(), command.getArgs());
            }

//            if(r.equals("OK")){
//                i=-10;
//                break;
//            }
//            i--;
//        }


//        System.out.println(r+":"+i);

        pool.release(redisClient);

        info.append(new String(command.getCommand()));
        info.append(":");
        for (byte[] arg : command.getArgs()) {
            info.append(" [");
            info.append(new String(arg));
            info.append("]");
        }

        info.append(" -> ");
        if(r instanceof Long ){
            info.append( r);
        }else if(r instanceof Integer){
            info.append(r);
        }else if (r instanceof byte[]){
            info.append(new String((byte[]) r));
        }else if(r instanceof String){
            
            info.append(r);
        }else {
            info.append(r);
        }


//        if(i!=-10){



//            info.append("error");
//            log.warn(info.toString());
//        }else {
//            info.append(r);
//            log.info(info.toString());
//        }
//


        return r;
    }
}
