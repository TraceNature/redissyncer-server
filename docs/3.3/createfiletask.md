## 创建文件任务
 syncer version ：3.3

### 数据文件支持类型
* AOF
* RDB
* MIXED
* ONLINEAOF(在线AOF)
* ONLINERDB(在线RDB)
* ONLINEMIXED(在线混合文件)

### 单节点到单节点

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v2/file/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "fileAddress": "/Users/test/Desktop/1.aof",
    "targetRedisAddress": "192.168.1.10:6379",
    "synctype":"AOF",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "redistest"
}'
```

### 单节点到cluster

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v2/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisType":"SINGLE",
    "sourceRedisAddress": "192.168.0.10:6379",
    "targetRedisAddress": "192.168.1.10:16379;192.168.1.10:16380;192.168.1.10:16381",
    "targetRedisType":"CLUSTER",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "redistest"
}'
```

### 多节点到单节点

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v2/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisType":"SINGLE",
    "sourceRedisAddress": "192.168.0.10:6380;192.168.0.10:6381;192.168.0.10:6382",
    "targetRedisAddress": "192.168.0.10:6379",
    "targetRedisType":"SINGLE",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "redistest"
}'
```

### 多节点到cluster

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v2/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisType":"SINGLE",
    "sourceRedisAddress": "192.168.0.10:6380;192.168.0.10:6381;192.168.0.10:6382",
    "targetRedisAddress": "192.168.1.10:16379;192.168.1.10:16380;192.168.1.10:16381",
    "targetRedisType":"CLUSTER",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "redistest"
}'
```



### DB库自定义映射

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v2/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisType":"SINGLE",
    "sourceRedisAddress": "192.168.0.10:6380;192.168.0.10:6381;192.168.0.10:6382",
    "targetRedisAddress": "192.168.1.10:16379;192.168.1.10:16380;192.168.1.10:16381",
    "targetRedisType":"CLUSTER",
    "dbMapper": {"1": "1"},
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "redistest"
}'
```

### 命令过滤

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v2/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisType":"SINGLE",
    "sourceRedisAddress": "192.168.0.10:6380;192.168.0.10:6381;192.168.0.10:6382",
    "targetRedisAddress": "192.168.1.10:16379;192.168.1.10:16380;192.168.1.10:16381",
    "targetRedisType":"CLUSTER",
    "dbMapper": {"1": "1"},
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "filterType":"COMMAND_FILTER_ACCEPT",
    "commandFilter":"SET,DEL,FLUSHALL",
    "taskName": "redistest"
}'
```


### key过滤

    {
        "sourceRedisAddress":"127.0.0.1:6379",
        "sourcePassword": "xxxxxx",
        "sourceRedisType":"SINGLE",
        "targetRedisType":"SINGLE",
        "targetRedisAddress": "127.0.0.1:6379",
        "targetPassword": "xxxxxx",
        "targetRedisVersion": 5,
        "taskName": "test",
        "filterType":"KEY_FILTER_ACCEPT",
        "keyFilter":"Redis(.*?)",
        "autostart":true
    }


#### 注意:
    * 源端为主从时只需填写主从节点中的一个节点即可，负责会造成主从数据同步两份
    * targetRedisType 当缺省时默认多节点为cluster单节点为single

### 创建任务字段

 * 字段描述

| field              | type               | example                                  | description                                                  | requred |
| ------------------ | ------------------ | ---------------------------------------- | ------------------------------------------------------------ | ------- |
| tasktype           | string             | "tasktype": "incrementonly"              | 任务类型，stockonly,incrementonly,total(只存量数据，只增量数据，存量＋增量);默认值total | false   |
| offsetPlace        | string             | "offsetPlace": "endbuffer"               | 当     tasktype为 incrementonly时，支持两种增量模式        "beginbuffer"、"endbuffer" 即从slave缓冲区的开头或结尾开始同步任务 | false   |
| dbMapper           | map<string,string> | "dbmapper": {"1": "1"}                   | redis db映射关系，当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库 | false   |
| sourceRedisType    | string             | "sourceRedisType":"SINGLE"               | 源节点集群类型SINGLE、CLUSTER                                |         |
| sourceRedisAddress | string             | "sourceRedisAddress": "10.0.0.1:6379"    | 源redis地址，cluster集群模式下地址由';'分割，如"10.0.0.1:6379;10.0.0.2:6379" | true    |
| sourcePassword     | string             | "sourcePassword": "sourcepasswd"         | 源redis密码默认值为""                                        | false   |
| targetRedisType    | string             | "targetRedisType": "SINGLE"              | 目标节点集群类型SINGLE、CLUSTER，目标proxy类型集群式可视为SINGLE |         |
| targetRedisAddress | string             | "targetRedisAddress": "192.168.0.1:6379" | 目标redis地址 ,当目标redis为单实例或proxy时，填写单一地址即可，当目标redis为集群且需要借助jedis访问集群时地址用';'分割，"192.168.0.1:6379;192.168.0.3:6379;192.168.0.3:6379" | true    |
| targetPassword     | string             | "targetPassword": "xxx"                  | 目标redis密码 ，默认值为""                                   | false   |
| targetRedisVersion | string             | "targetversion":"4.0"                    | 目标redis版本, 该参数针对不可获取版本信息的情况，若可获取redis版本信息则按自动获取的版本信息进行处理(保留2位小数) | false   |
| taskName           | string             | "taskname":"product2test"                | 自定义任务名称                                               | false   |
| autostart          | bool               | "autostart":true                         | 是否创建后自动启动，默认值false                              | false   |
| filterType         | string             | "filterType":"NONE"                      | 命令/key过滤类型                                             | false   |
| commandFilter      | string             | "commandFilter":"SET,DEL,FLUSHALL"       | 命令过滤器，不同命令间用,分割                                | false   |
| keyFilter          | string             | "keyFilter":"Redis(.*?)"                 | key名过滤器，需使用正则表达式                                | false   |


* filterType类型

| filterType类型         | type               |
|-----------------------|--------------------|
| NONE                  |  默认为NONE，过滤器不生效 |
| COMMAND_FILTER_ACCEPT | 只接受commandFilter中指定的命令，commandFilter中不同命令用,分割如[SET,DEL,FLUSHALL]，command大小写不敏感 |
| KEY_FILTER_ACCEPT     | 只接受keyFilter参数中key的数据（key大小写敏感），keyFilter需填写正则表达式如[Redis(.*?)] |
| COMMAND_AND_KEY_FILTER_ACCEPT  |  commandFilter和keyFilter同时生效 && 两者都满足放行 |
| COMMAND_OR_KEY_FILTER_ACCEPT   |  commandFilter和keyFilter指定的command 和key都接受 两者满足任意一者即生效放行 |
| COMMAND_FILTER_REFUSE          |  拒绝指定的commandFilter参数中指定的命令|
| KEY_FILTER_REFUSE              |  拒绝指定的keyFilter参数中指定的key|
| COMMAND_OR_KEY_FILTER_REFUSE   |  commandFilter和keyFilter指定的command 和key都接受 两者满足任意一者即拒绝|
| COMMAND_AND_KEY_FILTER_REFUSE  |  commandFilter和keyFilter同时生效 && 两者都满足拒绝|
