package syncer.syncerservice.util.taskutil.taskServiceQueue;

import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.entity.SqliteCommitEntity;
import syncer.syncerplusredis.model.BigKeyModel;
import syncer.syncerplusredis.model.DataCompensationModel;
import syncer.syncerplusredis.util.SqliteOPUtils;
import syncer.syncerservice.task.OffSetCommitEntity;
import syncer.syncerservice.util.common.Strings;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */
@Slf4j
public class DbDataCommitTask  implements Runnable{
    @Override
    public void run() {
        while (true){
            try {

                Object object=DbDataCommitQueue.take();
                SqliteCommitEntity sqliteCommitEntity=null;

                if(object instanceof OffSetCommitEntity){
                    OffSetCommitEntity data= (OffSetCommitEntity) object;
                    try {
                        SqliteOPUtils.updateOffsetAndReplId(data.getTaskId(),data.getOffset(),data.getReplId());
                    }catch (Exception e){
                        log.error("[{}]offset更新失败--->[{}]",data.getTaskId(),e.getMessage());
                    }

                    continue;
                }else  if(object instanceof SqliteCommitEntity){
                    sqliteCommitEntity= (SqliteCommitEntity) object;
                }else {
                    log.error("传入信息错误--->[{}]",object.getClass());
                }
                Integer type=sqliteCommitEntity.getType();
                if(type.equals(10)){
                    BigKeyModel bigKeyModel= (BigKeyModel) sqliteCommitEntity.getObject();
                    try {
                        SqliteOPUtils.insertBigKeyCommandModel(bigKeyModel);
                    }catch (Exception e){
                        log.error("大key统计入库失败：[{}]", Strings.toString(bigKeyModel.getCommand()));
                    }
                }else if(type.equals(20)){
                    DataCompensationModel dataCompensationModel= (DataCompensationModel) sqliteCommitEntity.getObject();
                    try{
                        SqliteOPUtils.insertDataCompensationModel(dataCompensationModel);
                    }catch (Exception e){
                        log.error("数据补偿记录写入失败[{}]:[{}]",dataCompensationModel.getCommand(),dataCompensationModel.getKey());
                        e.printStackTrace();
                    }
                }
//                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
