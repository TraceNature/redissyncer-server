package syncer.syncerpluswebapp.config.swagger.model;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/8/3
 */
public class GlobalString {
    @ApiSingleParam(value = "源节点地址", example = "127.0.0.1:6379")
    public static final String JSON_SOURCE_REDIS_ADDRESS = "sourceRedisAddress";
    @ApiSingleParam(value = "数据文件地址", example = "/usr/local/data/1.rdb")
    public static final String JSON_FILE_ADDRESS = "fileAddress";

    @ApiSingleParam(value = "文件类型", example = "RDB")
    public static final String JSON_FILE_TYPE= "fileType";

    @ApiSingleParam(value = "源节点密码", example = "test1234")
    public static final String JSON_SOURCE_REDIS_PASSWORD = "sourcePassword";

    @ApiSingleParam(value = "目标节点地址", example = "127.0.0.1:6380")
    public static final String JSON_TARGET_REDIS_ADDRESS = "targetRedisAddress";

    @ApiSingleParam(value = "目标节点密码", example = "test1234")
    public static final String JSON_TARGET_REDIS_PASSWORD = "targetPassword";

    @ApiSingleParam(value = "目标节点Redis版本", example = "4.0",type = Double.class)
    public static final String JSON_TARGET_REDIS_VERION= "targetRedisVersion";

    @ApiSingleParam(value = "任务名称", example = "testTask")
    public static final String JSON_TASKNAME= "taskName";

    @ApiSingleParam(value = "是否创建任务后自动启动(缺省为 false)", example = "true",type = Boolean.class)
    public static final String JSON_AUTO_START= "autostart";

    @ApiSingleParam(value = "提交批次大小(缺省为 500)", example = "500",type = Integer.class)
    public static final String JSON_BATCHSIZE= "batchSize";

    @ApiSingleParam(value = "db库映射关系", example = "'{0:1,1:2}'")
    public static final String JSON_DBMAPPER= "dbMapper";

    @ApiSingleParam(value = "taskids", example = "'['XXXXX1','XXXXX2']'",type=String.class)
    public static final String JSON_TASKIDS= "taskids";

    @ApiSingleParam(value = "groupIds", example = "'['XXXXX1','XXXXX2']'",type=String.class)
    public static final String JSON_TGROUPIDS= "groupIds";

    @ApiSingleParam(value = "是否从头开始（断点续传） 缺省为true", example = "true",type = Boolean.class)
    public static final String JSON_AUTO_AFRESH= "afresh";

    @ApiSingleParam(value = "data", example =" [\n" +
            "        {\n" +
            "            \"code\": \"2000\",\n" +
            "            \"taskId\": \"F70C4C70B8E64A349D2DC7AD22D01656\",\n" +
            "            \"groupId\": \"F70C4C70B8E64A349D2DC7AD22D01656\",\n" +
            "            \"msg\": \"Task created successfully and entered running state\",\n" +
            "            \"data\": null\n" +
            "        }\n" +
            "    ]")
    public static final String JSON_RESULT_DATA= "data";

    @ApiSingleParam(value = "code值", example = "2000")
    public static final String JSON_RESULT_CODE= "code";

    @ApiSingleParam(value = "msg值", example = "The request is successful")
    public static final String JSON_RESULT_MSG= "msg";


    @ApiSingleParam(value = "data", example =" [\n" +
            "        {\n" +
            "            \"code\": \"2000\",\n" +
            "            \"taskId\": \"F70C4C70B8E64A349D2DC7AD22D01656\",\n" +
            "            \"msg\": \"Task stopped successfully\",\n" +
            "        }\n" +
            "    ]")
    public static final String JSON_STOPTASK_RESULT_DATA= "data";


    @ApiSingleParam(value = "data", example =" [\n" +
            "        {\n" +
            "            \"code\": \"2000\",\n" +
            "            \"taskId\": \"F70C4C70B8E64A349D2DC7AD22D01656\",\n" +
            "            \"msg\": \"OK\",\n" +
            "        }\n" +
            "    ]")
    public static final String JSON_STARTASK_RESULT_DATA= "data";


    @ApiSingleParam(value = "查询规则", example = "'all ['bynames','all','byids','bystatus','byGroupIds']'")
    public static final String JSON_SELECT_TASKNAME= "regulation";

    @ApiSingleParam(value = "tasknames", example = "'['XXXXX1','XXXXX2']'")
    public static final String JSON_TASKNAMES= "tasknames";
    @ApiSingleParam(value = "tasknames", example = "'['XXXXX1','XXXXX2']'")
    public static final String JSON_TASKSTATUS= "taskstatus";



    @ApiSingleParam(value = "data", example ="'[\n" +
            "        {\n" +
            "            \"taskId\": \"9D9DA1AC971E45CF9A8732F11CA00063\",\n" +
            "            \"groupId\": \"9D9DA1AC971E45CF9A8732F11CA00063\",\n" +
            "            \"taskName\": \"在线同步1234\",\n" +
            "            \"sourceRedisAddress\": \"114.67.100.239:6379\",\n" +
            "            \"targetRedisAddress\": \"114.67.100.240:6379\",\n" +
            "            \"fileAddress\": \"\",\n" +
            "            \"autostart\": true,\n" +
            "            \"afresh\": true,\n" +
            "            \"batchSize\": 1000,\n" +
            "            \"tasktype\": \"TOTAL\",\n" +
            "            \"offsetPlace\": \"ENDBUFFER\",\n" +
            "            \"taskMsg\": \"增量同步开始\",\n" +
            "            \"offset\": 2538985,\n" +
            "            \"status\": \"BROKEN\",\n" +
            "            \"redisVersion\": 5.0,\n" +
            "            \"rdbVersion\": 9,\n" +
            "            \"syncType\": \"SYNC\",\n" +
            "            \"sourceRedisType\": \"SINGLE\",\n" +
            "            \"targetRedisType\": \"SINGLE\",\n" +
            "            \"dbMapper\": null,\n" +
            "            \"analysisMap\": {\n" +
            "                \"ZSET\": 1,\n" +
            "                \"KeyValue总数\": 16,\n" +
            "                \"KeyValue总数(包括分片)\": 9,\n" +
            "                \"millis2String\": \"2020-07-31 15:47:17\",\n" +
            "                \"STRING\": 7,\n" +
            "                \"FRAGMENTATION_NUM\": 9,\n" +
            "                \"time\": 1596181637898,\n" +
            "                \"LIST\": 4,\n" +
            "                \"HASH\": 4\n" +
            "            },\n" +
            "            \"createTime\": \"2020-07-31 15:47:15\",\n" +
            "            \"updateTime\": \"2020-07-31 15:47:21\",\n" +
            "            \"replId\": \"ad6ae7c1febab76015de8b5f825d2c5333a47092\",\n" +
            "            \"rdbKeyCount\": 16,\n" +
            "            \"allKeyCount\": 7,\n" +
            "            \"realKeyCount\": 7,\n" +
            "            \"commandKeyCount\": -9,\n" +
            "            \"rate\": 0.4375,\n" +
            "            \"rate2Int\": 44,\n" +
            "            \"lastDataUpdateIntervalTime\": 1596424410409,\n" +
            "            \"lastDataCommitIntervalTime\": 1596424410409\n" +
            "        }\n" +
            "    ]'")
    public static final String JSON_LISTS_RESULT_DATA= "data";


    @ApiSingleParam(value = "data", example =" '[\n" +
            "        {\n" +
            "            \"code\": \"2000\",\n" +
            "            \"taskId\": \"F70C4C70B8E64A349D2DC7AD22D01656\",\n" +
            "            \"msg\": \"OK\",\n" +
            "        }\n" +
            "    ]'")
    public static final String JSON_REMOVETASK_RESULT_DATA= "data";
}
