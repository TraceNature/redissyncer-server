package syncer.syncerservice.task;

import org.apache.ibatis.annotations.Mapper;
import syncer.syncerplusredis.dao.AbandonCommandMapper;
import syncer.syncerservice.constant.DbCommitTypeEnum;

/**
 * @author zhanenqiang
 * @Description 数据库提交线程
 * @Date 2020/7/6
 */
public class DbCommitTask implements Runnable{
    @Mapper
    private AbandonCommandMapper abandonCommandMapper;

    private DbCommitTypeEnum type;

    public DbCommitTask(DbCommitTypeEnum type) {
        this.type = type;
    }

    @Override
    public void run() {
        while (true){
            if(type.equals(DbCommitTypeEnum.AbandonCommand)){
//                abandonCommandMapper.insertAbandonCommandModel()
            }
        }

    }
}
