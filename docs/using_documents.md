## RedisSyncer简介
RedisSyncer一款通过replication协议模拟slave来获取源Redis节点数据并写入目标Redis从而实现数据同步的Redis同步工具
### 同步基本流程如下
![avatar](images/flow_path.png)    

### 基本功能
* 在线同步(支持全量和增量，基于offset实现断点续传)
* 数据文件导入(支持rdb aof mixed)
* 增量命令记录(生成aof文件)
* 命令/key过滤
* db映射 (0库数据->目标1库)

### 使用前提
* 使用在线同步模式源端Redis节点需支持SYNC、PSYNC命令，如果云redis proxy不支持sync命令或无法连接proxy后面的真实节点时，则无法使用在线同步,只能通过数据文件导入
* 必须能够确保syncer能够连接 源Redis节点、目标Redis节点/proxy
* 确保源Redis命令以及协议兼容社区版Redis2.8-6.0版本
* 只需将主从节点的其中一者作为源节点即可，否则会出现非幂等类型数据写入两次
* repl-ping-slave-period要小于readTimeout（redissyncer默认60000ms）
* 确保源redis节点内存足够，能够保证bgsave操作正常执行
* 目标端目前支持cluster、单机、代理分片集群等模式
* 迁移前确保目标库为空没有垃圾数据，防止造成数据不一致  
* 在迁移过程中尽量避免目标端有其他客户端写入

### 存储类型
 * sqlite(默认)
 * etcd 
   
若切换为etcd请修改 application.yml 中server.storageType 为etcd 并配置 etcd参数

### 创建任务参数说明(*重要)
#### 请求地址
    http://{ip}:{port}/api/v1/creattask

    Method:POST

    Content-Type:application/json
#### 请求参数说明

|   参数                  | 说明	                             | 示例                                             | 可缺省 |  
|------------------------|-----------------------------------|-------------------------------------------------|---|  
| sourceRedisAddress     |源Redis节点/集群的连接地址与服务端口,多个用;分割，每个节点同步工具都会创建一个任务，确保主从只填主或从节点一者的信息，否则会造成数据同步重复 | "sourceRedisAddress": "127.0.0.1:6379;127.0.0.1:6340"  |否|  
| sourcePassword         |源redis节点密码,若存在多节点则需要保证多个节点密码一致  |      redistest0102 |是|
| targetRedisAddress     |目标Redis节点/集群的连接地址与服务端口,多个用;分割，当为多个节点是工具是会以cluster模式进行连接 | "targetRedisAddress": "192.168.0.1:6379" |否|  
| targetPassword |目标redis节点/集群密码，没有密码可缺省|redistest0102|是|  
|targetRedisVersion | 目标Redis版本，必填，默认会自动获取，获取失败会使用本参数，本参数为兜底参数,保留小数点后一位 | 4.0 |否|  
|taskName|自定义任务名称，用来区分任务以及按任务名查询| "taskname":"product2test"	 |否|
|autostart|创建任务后是否自动启动，默认为false，若为true则调用本接口后任务将自动启动，无需调用start接口 | "autostart":true	|否|
|afresh|缺省为true， 是否从头开始，若为false，则同步任务将会获取全量+增量数据，若为false则表示只增量不全量，同步工具将会从源redisbacklog最后一个offset+1开始同步|"afresh":true	|是|
|filterType| 过滤器类型，默认为none，具体类型请查看下列列表|"filterType":"NONE"|是|
|commandFilter|命令过滤器，与filterType配合使用，不同命令之间用,分割|"commandFilter":"SET,DEL,FLUSHALL"	|是|
|keyFilter|key过滤器，与filterType配合使用，需使用正则表达式匹配key|"keyFilter":"Redis(.*?)"	|是|


* filterType类型

| filterType类型                  | type               |
|--------------------------------|--------------------|
| NONE                           |  默认为NONE，过滤器不生效 |
| COMMAND_FILTER_ACCEPT          | 只接受commandFilter中指定的命令，commandFilter中不同命令用,分割如[SET,DEL,FLUSHALL]，command大小写不敏感 |
| KEY_FILTER_ACCEPT              | 只接受keyFilter参数中key的数据（key大小写敏感），keyFilter需填写正则表达式如[Redis(.*?)] |
| COMMAND_AND_KEY_FILTER_ACCEPT  |  commandFilter和keyFilter同时生效 && 两者都满足放行 |
| COMMAND_OR_KEY_FILTER_ACCEPT   |  commandFilter和keyFilter指定的command 和key都接受 两者满足任意一者即生效放行 |
| COMMAND_FILTER_REFUSE          |  拒绝指定的commandFilter参数中指定的命令|
| KEY_FILTER_REFUSE              |  拒绝指定的keyFilter参数中指定的key|
| COMMAND_OR_KEY_FILTER_REFUSE   |  commandFilter和keyFilter指定的command 和key都接受 两者满足任意一者即拒绝|
| COMMAND_AND_KEY_FILTER_REFUSE  |  commandFilter和keyFilter同时生效 && 两者都满足拒绝|

### 常见问题

1.当源为主从时，源节点应该填主节点还是从节点
  * 当进行全量数据同步时，redis节点会进行bgsave操作和rdb的网络传输，开销会比较大，因此推荐将从节点作为源，以降低master的压力 
  * 主从节点只需任意一个节点作为源，负责会造成数据同步重复

2.如何断点续传
  * 调用start接口时 afresh参数设置为false

3.出现EOFException可能原因 
   * repl-ping-slave-period要小于readTimeout（redissyncer默认60000ms）
   * 源节点内存不够无法进行bgsave
   * 续传offset刷过

### 断点续传机制
3.3以上版本有两种断点续传机制 v1、v2
* v1 基于offset实现简陋版本的断点续传，即将offset持久化到本地，当出现程序突然宕掉可能会导致最新的offset无法持久化,进而可能造成增量续传阶段offset不为最新导致部分命令二次拉取,但该版本机制不会在目标redis写入记录数据
* v2 增强版断点续传机制，每次命令提交syncer会自动将每一批次数据封装成一个事务，并在每个事务中加入一个key为 syncer-hash-offset-checkpoint的检查点写入目标库，能够尽最大可能满足数据一致性。但本机制会忘目标redis每个存有数据的db中插入一条名为syncer-hash-offset-checkpoint的hash结构记录相关数据

#### 如何开启v2
* 默认为v1,若想使用v2断点续传机制，请在启动syncer时设置  --server.breakpointContinuationType=v2 

