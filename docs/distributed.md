# 分布式设计

## 主要组件

* redisyncer-portal
  * 任务调度
  * 健康检查
  * RBAC 
  * 对外提供restful接口
* redisyncer-server
  * 运行具体同步任务
  * 向etcd注册服务
  * 上报健康状况 
* etcd

## etcd中存储的数据结构

| prefix |key 或 key的编码规则| value | description|
| ---| ---| ---|---|
|/uniqid|	idseed	|uint64	|全局唯一id种子，初始化为1，每次加锁递增1|
| /inspect| lastinspectiontime | unix时间戳|最后巡检的时间|
| /inspect| execlock | |巡检执行的分布式锁|
| /nodes/| {nodetype}/{nodeID} | {"heartbeaturl":"/health","lastreporttime":1615431908432,"nodeaddr":"127.0.0.1","nodeid":"1","nodeport":8082,"nodetype":"redissyncernodeserver","online":true}|已注册的node|
| /tasks/taskid/| {taskid} | taskstatusjson|任务信息|
| /tasks/node/|{nodeId}/{taskId}|{"nodeId":"xxx","taskId":"xxx"}|nodeId下的任务信息|
| /tasks/groupid/|{groupid}/{taskId}|{"groupId":"xxx","taskId":"xxx"}|groupId下的任务列表|
| /tasks/status/| {currentstatus}/{taskid} | {"taskId":"testId"}|任务当前状态信息|
| /tasks/rdbversion/|{redisVersion}/{rdbVersion}| {"id":1,"redis_version": "2.6","rdb_version": 6}|rdb-redis version映射关系|
| /tasks/offset/|{taskId}|{"replId":"xxx","replOffset":"-1"}|任务offset信息|
| /tasks/name/|{taskname}/{taskId}|{"taskId":"testId"}|用于根据taskName查询任务信息|
| /tasks/type/|{type}/{taskId}|{"taskid":"xxx","groupId":"xxx","nodeId":"xxx"}|根据任务类型获取任务信息|
| /tasks/user/|{username}|{"id":1,"username":"xxx","name":"xxx","password":"xxx","salt":"xxx"}|用户账号信息|
| /tasks/bigkey/|{taskId}/{bigKey}| {"id":1,"taskId":"xxx","command":"xxx","command_type":"xxx"}|任务大key记录信息|
| /tasks/md5/|{md5}|{"taskid":"xxx","groupId":"xxx","nodeId":"xx"}|任务md5信息|
| /tasks/compensation/ |{taskId}/{compensationId}|{"id":1,"taskId":"xxx","groupId":"xxx","command":"xxx","value":"xxx","key":"xxx","times":3,"createTime","xxx"} |命令进入数据补偿的次数以及信息 |
| /tasks/compensation/ |{groupId}/{compensationId}|{"compensationId": 1,"taskId":"xxx"}|根据groupId 查询进入数据补偿的key|
| /tasks/abandon/|{taskId}/{abandonId}|{"id":1,"taskId":"xxx","groupId":"xxx","command":"xxx","key":"xxx","value":"xxx","type":1,"ttl":1000,"exception":"xxx","result":"xxx","desc":"xxx","createTime":"xxx"}|被抛弃command记录|
| /tasks/abandon/|{groupId}/{abandonId} |{"abandonId": 1,"taskId":"xxx"}|根据groupId查询被抛弃key|
| /tasks/lastkeyacross/|{taskId} |{"lastKeyCommitTime": 1,"lastKeyUpdateTime": 1,"taskId":"xxx","groupId":"xxx"}|根据taskId查询key lastCommitTime和lastUpdateTime|


### 任务状态

| TYPE | code | description| status |
| ---| ---|---|---|
|STOP     |0|任务停止| 已使用 |
|CREATING |1|创建中 |  已使用 |
|CREATED  |2|创建完成| 已使用 |
|RUN      |3|运行状态| 已使用 |
|BROKEN   |5|任务异常| 已使用 |
|RDBRUNING|6|全量RDB同步过程中| 已使用 |
|COMMANDRUNING|7|增量同步中| 已使用 |
|FINISH    |8|完成状态| 未使用(备用) |

### 任务类型

| TYPE | code | description| status |
| ---| ---|---|---|
|SYNC |1|replication| 已使用 |
|RDB  |2|RDB文件解析 |  已使用 |
|AOF  |3|AOF文件解析| 已使用 |
|MIXED|4|混合文件解析| 已使用 |
|ONLINERDB |5|在线RDB解析| 已使用 |
|ONLINEAOF |6|在线AOF| 已使用 |
|ONLINEMIXED|7|在线混合文件解析| 已使用 |
|COMMANDDUMPUP|8|增量命令实时备份| 已使用 |

## 任务信息详情

| NAME                | type     | description     |
| --------------------| ---------|-----------------|
|nodeId               | string   |  节点id          |
|id                   | string   |  任务Id          |
|taskId               | string   |  任务Id          |
|groupId              | string   |  任务组Id        |
|taskName             | string   |  任务名称         |
|sourceRedisAddress   | string   |  源RedisUri     |
|sourcePassword       | string   |  源Redis密码     |
|targetRedisAddress   | string   |  目标RedisUri    |
|targetPassword       | string   |  目标Redis密码    |
|fileAddress          | string   |  文件地址         |
|autostart            | bool     |创建任务时是否自动启动|
|afresh               | bool     |重新启动时从头开始为 true 续传为false                 |
|batchSize            | int      |批次大小 默认为 1500 |
|tasktype             | int      | 任务类型          |
|offsetPlace          | int      | 增量模式下从缓冲区开始同步的位置|
|taskMsg              | string   | 任务反馈信息      |
|offset               | long     | offset          |
|status               | int      | 任务状态         |
|redisVersion         | double   | redis版本        |
|rdbVersion           | int      | rdb版本          |
|syncType             | int      | 数据同步类型      |
|sourceRedisType      | int      | 源Redis类型      |
|targetRedisType      | int      | 目标Redis类型    |
|sourceHost           | string   |                 |
|targetHost           | string   |                 |
|sourcePort           | int      |                 |
|targetPort           | int      |                 |
|updateTime           | string   |                 |
|md5                  | string   |                 |
|dataAnalysis         | string   | 全量数据分析结果   |
|replId               | string   |                 |
|rdbKeyCount          | long     |                 |
|allKeyCount          | long     |                 |
|realKeyCount         | long     |                 |
|lastKeyUpdateTime    | long     |                 |
|sourceAcl            | bool     |                 |
|targetAcl            | bool     |                 |
|sourceUserName       | string   |                 |
|targetUserName       | string   |                 |
|errorCount           | int      |                 |
|expandJson           | string   |                 |
|timeDeviation        | long     |                 |
|filterType           | string   |                 |
|commandFilter        | string   |                 |
|keyFilter            | string   |                 |
## id规范

* clusterId
  cluster_自定义，条件是系统内不重复
* nodeId
  node_自定义，条件是系统内不重复
* groupId
  goup_全局唯一id
* taskId
  task_全局唯一id
* 全局唯一id生成
  * 全局唯一id=idseed_本地unix时间戳(毫秒13位)_从1开始的自然数序列
  * idseed 存储于etcd中的全局唯一key， 为自增unit64，初始化为1，每次加锁+1

## 节点注册过程

* portal和redissyncer节点在配置文件中配置nodeid，portal解点检查/node/portal/下无节点信息则写入节点信息，修改节点状态为online；若有则检查节点状态是否为offline，若为online则注册失败，上报当前节点状态；redissyncer节点 检查 /node/redissyncer/下若无节点信息则写入节点信息，若有则注册失败
  
## 节点健康检查规则

* portal 执行单独的协程负责集群健康检查工作
  * watch key "/inspect/lastinspectiontime"
  * 定期发起健康检查流程，当健康检查状态为false时，设置为true，当状态为true时执行健康检查
  * 当key "/inspect/lastinspectiontime"发生变更时，讲检查状态设置为false
  * 执行健康检查时，先将key "/inspect/execlock" 加锁，然后执行健康检查
  * 当其他进程发现key "/inspect/execlock"已加锁时，变更key "/inspect/lastinspectiontime" 为最新时间戳后退出检查

* 检查项
  * 节点健康
    * 调用redissyncer节点health接口，若三次失败则确定节点宕机；配置节点为离线状态，将节点上 ”immortal“ 属性的任务重新调度到其他节点并尝试启动
  * 任务健康
    * 查找任务状态为broken 的 任务，发送告警通知


## 任务创建规则（待细化）

* 前期没有好的方法可以根据任务数量选择任务数最少的节点，由server节点判断资源是否支持任务，若资源不满足则任务失败，调度重新选择节点
* 节点选择：节点定期上报活动任务数及其资源占用比例；选择资源占用最少且任务数量最少的节点创建任务

## 节点离线规则
