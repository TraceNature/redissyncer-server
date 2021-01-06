package syncer.transmission.strategy.taskcheck.impl;

import syncer.common.constant.ResultCodeAndMessage;
import syncer.common.exception.TaskMsgException;
import syncer.replica.entity.SyncType;
import syncer.replica.util.SyncTypeUtils;
import syncer.transmission.client.RedisClient;
import syncer.transmission.model.TaskModel;
import syncer.transmission.strategy.taskcheck.ITaskCheckStrategy;
import syncer.transmission.util.code.CodeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author zhanenqiang
 * @Description 校验在线文件地址是否为网址  即http https文件是否存在
 * @Date 2020/12/14
 */
public class TaskOnlineAddressCheckStrategy implements ITaskCheckStrategy {
    private ITaskCheckStrategy next;
    private RedisClient client;
    private TaskModel taskModel;

    @Override
    public void run(RedisClient client, TaskModel taskModel) throws Exception {
        SyncType res= SyncTypeUtils.getSyncType(taskModel.getSyncType());
        if(SyncType.ONLINEMIXED.equals(res)||SyncType.ONLINERDB.equals(res)||SyncType.ONLINEAOF.equals(res)){
            if(isNetFileAvailable(taskModel.getFileAddress())){
                //下一节点
                toNext(client,taskModel);
            }else{
                throw new TaskMsgException(CodeUtils.codeMessages(ResultCodeAndMessage.TASK_MSG_ONLINEFILE_ADDRESS_ERROR.getCode(),ResultCodeAndMessage.TASK_MSG_ONLINEFILE_ADDRESS_ERROR.getMsg()));
            }
        }else{
            toNext(client,taskModel);
        }
    }

    @Override
    public void toNext(RedisClient client, TaskModel taskModel) throws Exception {
        if(null!=next) {
            next.run(client,taskModel);
        }
    }

    @Override
    public void setNext(ITaskCheckStrategy nextStrategy) {
        this.next=nextStrategy;
    }

    /**
     * 判断是否是在线文件
     * @param strUrl
     * @return
     */
    boolean isNetFileAvailable(String strUrl){
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
                e.printStackTrace();
            }
        }
    }
}
