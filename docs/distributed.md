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
| /nodes/| nodetype/nodeID | 节点状态，由节点上报|已注册的node|
| /tasks/taskid| taskid | taskstatusjson|巡检执行的分布式锁|
| /tasks/node/| nodeId/groupid | taskidset|巡检执行的分布式锁|
| /tasks/groupid/| groupid | taskidset|巡检执行的分布式锁|
| /tasks/status/| currentstatus/taskid | taskidset|巡检执行的分布式锁|

