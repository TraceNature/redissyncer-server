package syncer.syncerservice.filter.redis_start_check_strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import syncer.syncerpluscommon.constant.ResultCodeAndMessage;
import syncer.syncerplusredis.constant.SyncType;
import syncer.syncerplusredis.entity.RedisPoolProps;
import syncer.syncerplusredis.exception.TaskMsgException;
import syncer.syncerplusredis.model.TaskModel;
import syncer.syncerplusredis.util.SyncTypeUtils;
import syncer.syncerplusredis.util.code.CodeUtils;
import syncer.syncerservice.util.JDRedisClient.JDRedisClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author zhanenqiang
 * @Description 校验在线文件地址是否为网址  即http https文件是否存在
 * @Date 2020/7/16
 */
@AllArgsConstructor
@Builder
public class SyncerOnlineAddressCheckStrategy implements IRedisStartCheckBaseStrategy{
    private IRedisStartCheckBaseStrategy next;
    private JDRedisClient client;
    private TaskModel taskModel;
    private RedisPoolProps redisPoolProps;

    @Override
    public void run(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {
        SyncType res=SyncTypeUtils.getSyncType(taskModel.getSyncType());
        if(SyncType.ONLINEMIXED.equals(res)||SyncType.ONLINERDB.equals(res)||SyncType.ONLINEAOF.equals(res)){
            if(isNetFileAvailableBIG(taskModel.getFileAddress())){
                //下一节点
                toNext(client,taskModel,redisPoolProps);
            }else{
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_ONLINEFILE_ADDRESS_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_ONLINEFILE_ADDRESS_ERROR.getMsg()));


            }
        }else{
            toNext(client,taskModel,redisPoolProps);
        }
    }

    @Override
    public void toNext(JDRedisClient client, TaskModel taskModel, RedisPoolProps redisPoolProps) throws Exception {
        if(null!=next) {
            next.run(client,taskModel,redisPoolProps);
        }
    }

    @Override
    public void setNext(IRedisStartCheckBaseStrategy nextStrategy) {
        this.next=nextStrategy;
    }


     boolean isNetFileAvailable(String strUrl) {
        URL url = null;
        try {
            url = new URL(strUrl);
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            String message = urlcon.getHeaderField(0);
            //文件存在‘HTTP/1.1 200 OK’ 文件不存在 ‘HTTP/1.1 404 Not Found’
//            Long TotalSize=Long.parseLong(urlcon.getHeaderField("Content-Length"));
            if (message.indexOf("HTTP/1.1 200 OK")>=0){
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    boolean isNetFileAvailableBIG(String strUrl) {
        InputStream netFileInputStream = null;
        try {
            URL url = new URL(strUrl);
            URLConnection urlConn = url.openConnection();
            netFileInputStream = urlConn.getInputStream();
            if (null != netFileInputStream) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (netFileInputStream != null){
                    netFileInputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }


}
