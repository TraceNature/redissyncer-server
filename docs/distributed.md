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