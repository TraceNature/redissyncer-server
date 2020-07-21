package syncer.syncerpluswebapp;

import com.alibaba.fastjson.JSON;


import org.apache.ibatis.annotations.Mapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import syncer.syncerpluscommon.util.spring.SpringUtil;
import syncer.syncerplusredis.dao.AbandonCommandMapper;
import syncer.syncerplusredis.dao.RdbVersionMapper;
import syncer.syncerplusredis.dao.TaskMapper;
import syncer.syncerplusredis.model.AbandonCommandModel;
import syncer.syncerplusredis.model.TaskModel;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITest {
    @Autowired
    TaskMapper testMaapper;

    @Autowired
    AbandonCommandMapper abandonCommandMapper;

//    @Autowired
//    RdbVersionMapper rdbVersionMapper;

    @Test
    public void abandonCommandMapperTest() throws Exception {
        abandonCommandMapper.insertSimpleAbandonCommandModel(AbandonCommandModel
                .builder()
                .command("command")
                .exception("null")
                .key("command")
                .taskId("command")
                .desc("command")
                .build());
    }


    @Test
    public void testing() throws Exception {
        List<TaskModel>taskModelList=new ArrayList<>();
//        List<RdbVersionModel>rdbVersionModelList=new ArrayList<>();
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("2")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("2.6")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("2.8")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("3")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("3.0")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("3.2")
//                .rdb_version(7)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("4.0")
//                .rdb_version(8)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("4")
//                .rdb_version(8)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("5.0")
//                .rdb_version(9)
//                .build());
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("5")
//                .rdb_version(9)
//                .build());
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("jimdb_3.2")
//                .rdb_version(6)
//                .build());
//
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("jimdb_4.0")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("jimdb_4.1")
//                .rdb_version(6)
//                .build());
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("jimdb_5.0")
//                .rdb_version(6)
//                .build());
//
//        rdbVersionModelList.add(RdbVersionModel.builder()
//                .redis_version("jimdb")
//                .rdb_version(6)
//                .build());


        taskModelList.add(TaskModel.builder()
                .id("testId")
                .groupId("pdategroupId")
                .taskName("pdatetaskName")
                .afresh(false)
                .autostart(false)
                .batchSize(5000)
                .sourceRedisAddress("pdatesourceAddress")
                .sourcePassword("pdatesourcePassword")
                .targetRedisAddress("pdatetargetAddress")
                .targetPassword("pdatetargetPassword")
                .offset(3000L)
                .offsetPlace(1000)
                .taskMsg("pdatemsg")
                .tasktype(11)
                .status(2)
                .build());


        taskModelList.add(TaskModel.builder()
                .id("testId1")
                .groupId("pdategroupId")
                .taskName("pdatetaskName")
                .afresh(false)
                .autostart(false)
                .batchSize(5000)
                .sourceRedisAddress("pdatesourceAddress")
                .sourcePassword("pdatesourcePassword")
                .targetRedisAddress("pdatetargetAddress")
                .targetPassword("pdatetargetPassword")
                .offset(3000L)
                .offsetPlace(1000)
                .taskMsg("pdatemsg")
                .tasktype(11)
                .status(2)
                .build());

//        taskModelList.add(TaskModel.builder()
//                .id("testId1")
//                .groupId("updategroupId1")
//                .taskName("updatetaskName")
//                .afresh(false)
//                .autostart(false)
//                .batchSize(2000)
//                .sourceRedisAddress("updatesourceAddress")
//                .sourcePassword("updatesourcePassword")
//                .targetRedisAddress("updatetargetAddress")
//                .targetPassword("updatetargetPassword")
//                .offset(1000L)
//                .offsetPlace(5)
//                .taskMsg("msg")
//                .tasktype(10)
//                .status(1)
//                .build());
//        System.out.println(testMaapper.insertTask(TaskModel.builder()
//                .id("testId")
//                .groupId("updategroupId")
//                .taskName("updatetaskName")
//                .afresh(false)
//                .autostart(false)
//                .batchSize(2500)
//                .sourceRedisAddress("updatesourceAddress")
//                .sourcePassword("updatesourcePassword")
//                .targetRedisAddress("updatetargetAddress")
//                .targetPassword("updatetargetPassword")
//                .offset(1000L)
//                .offsetPlace(5)
//                .taskMsg("updatemsg")
//                .tasktype(10)
//                .status(1)
//                .build()))
//                sout;
        RdbVersionMapper rdbVersionMapper= SpringUtil.getBean(RdbVersionMapper.class);
        System.out.println(JSON.toJSONString(rdbVersionMapper.findRdbVersionModelByRedisVersion("4.0")));
//        System.out.println(JSON.toJSONString(testMaapper.updateTaskList(taskModelList)));
    }
}
