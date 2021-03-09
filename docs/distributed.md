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
| /nodes/| {nodetype}/{nodeID} | 节点状态，由节点上报|已注册的node|
| /tasks/taskid/| {taskid} | taskstatusjson|任务信息|
| /tasks/node/|{nodeId}/{taskId}|{"nodeId":"xxx","taskId":"xxx"}|nodeId下的任务信息|
| /tasks/groupid/|{groupid}/{taskId}|{"groupId":"xxx","taskId":"xxx"}|groupId下的任务列表|
| /tasks/status/| {currentstatus}/{taskid} | {"taskId":"testId"}|任务当前状态信息|
| /tasks/rdbversion/|{redisVersion}/{rdbVersion}| {"id":1,"redis_version": "2.6","rdb_version": 6}|rdb-redis version映射关系|
| /tasks/offset/|{taskId}|{"replId":"xxx","replOffset":"-1"}|任务offset信息|
| /tasks/name/|{taskname}|{"taskId":"testId"}|用于根据taskName查询任务信息|
| /tasks/type/|{type}/{taskId}|{"taskid":"xxx","groupId":"xxx","nodeId":"xxx"}|根据任务类型获取任务信息|
| /tasks/user/|{username}|{"id":1,"username":"xxx","name":"xxx","password":"xxx","salt":"xxx"}|用户账号信息|
| /tasks/bigkey/|{taskId}/{bigKey}| {"id":1,"taskId":"xxx","command":"xxx","command_type":"xxx"}|任务大key记录信息|
| /tasks/md5/|{md5}|{"taskid":"xxx","groupId":"xxx","nodeId":"xx"}|任务md5信息|
|||||

###任务状态

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
