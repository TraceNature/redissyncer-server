package syncer.syncerpluswebapp;

import com.alibaba.fastjson.JSON;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import syncer.syncerservice.dao.RdbVersionMapper;
import syncer.syncerservice.dao.TaskMapper;
import syncer.syncerservice.model.RdbVersionModel;
import syncer.syncerservice.model.TaskModel;

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
    RdbVersionMapper rdbVersionMapper;
    @Test
    public void testing() throws Exception {
        List<TaskModel>taskModelList=new ArrayList<>();
        List<RdbVersionModel>rdbVersionModelList=new ArrayList<>();


        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("2")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("2.6")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("2.8")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("3")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("3.0")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("3.2")
                .rdb_version(7)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("4.0")
                .rdb_version(8)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("4")
                .rdb_version(8)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("5.0")
                .rdb_version(9)
                .build());
        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("5")
                .rdb_version(9)
                .build());
        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("jimdb_3.2")
                .rdb_version(6)
                .build());


        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("jimdb_4.0")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("jimdb_4.1")
                .rdb_version(6)
                .build());
        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("jimdb_5.0")
                .rdb_version(6)
                .build());

        rdbVersionModelList.add(RdbVersionModel.builder()
                .redis_version("jimdb")
                .rdb_version(6)
                .build());


//        taskModelList.add(TaskModel.builder()
//                .id("testId")
//                .groupId("groupId")
//                .taskName("taskName")
//                .afresh(false)
//                .autostart(false)
//                .batchSize(1000)
//                .sourceRedisAddress("sourceAddress")
//                .sourcePassword("sourcePassword")
//                .targetRedisAddress("targetAddress")
//                .targetPassword("targetPassword")
//                .offset(1000L)
//                .offsetPlace(5)
//                .taskMsg("msg")
//                .tasktype(10)
//                .status(1)
//                .build());
//
//
//        taskModelList.add(TaskModel.builder()
//                .id("testId1")
//                .groupId("groupId1")
//                .taskName("taskName")
//                .afresh(false)
//                .autostart(false)
//                .batchSize(1000)
//                .sourceRedisAddress("sourceAddress")
//                .sourcePassword("sourcePassword")
//                .targetRedisAddress("targetAddress")
//                .targetPassword("targetPassword")
//                .offset(1000L)
//                .offsetPlace(5)
//                .taskMsg("msg")
//                .tasktype(10)
//                .status(1)
//                .build());
//        System.out.println(testMaapper.insertTask(TaskModel.builder()
//                .id("testId")
//                .groupId("groupId")
//                .taskName("taskName")
//                .afresh(false)
//                .autostart(false)
//                .batchSize(1000)
//                .sourceRedisAddress("sourceAddress")
//                .sourcePassword("sourcePassword")
//                .targetRedisAddress("targetAddress")
//                .targetPassword("targetPassword")
//                .offset(1000L)
//                .offsetPlace(5)
//                .taskMsg("msg")
//                .tasktype(10)
//                .status(1)
//                .build()));
        System.out.println(JSON.toJSONString(rdbVersionMapper.insertRdbVersionModelList(rdbVersionModelList)));
    }
}
