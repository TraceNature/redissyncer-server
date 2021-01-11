// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.task;

import lombok.extern.slf4j.Slf4j;
import syncer.replica.util.objectutil.Strings;
import syncer.transmission.entity.SqliteCommitEntity;
import syncer.transmission.model.BigKeyModel;
import syncer.transmission.model.DataCompensationModel;
import syncer.transmission.po.entity.OffSetCommitEntity;
import syncer.transmission.queue.DbDataCommitQueue;
import syncer.transmission.util.sql.SqlOPUtils;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
@Slf4j
public class DbDataCommitTask  implements Runnable{
    @Override
    public void run() {
        while (true){
            try {
                Object object= DbDataCommitQueue.take();
                SqliteCommitEntity sqliteCommitEntity=null;
                if(object instanceof OffSetCommitEntity){
                    OffSetCommitEntity data= (OffSetCommitEntity) object;
                    try {
                        SqlOPUtils.updateOffsetAndReplId(data.getTaskId(),data.getOffset(),data.getReplId());
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
                        SqlOPUtils.insertBigKeyCommandModel(bigKeyModel);
                    }catch (Exception e){
                        log.error("大key统计入库失败：[{}]", Strings.toString(bigKeyModel.getCommand()));
                    }
                }else if(type.equals(20)){
                    DataCompensationModel dataCompensationModel= (DataCompensationModel) sqliteCommitEntity.getObject();
                    try{
                        SqlOPUtils.insertDataCompensationModel(dataCompensationModel);
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
