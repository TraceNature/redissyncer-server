### RedisSyncer引擎的设计与实现
RedisSyncer一款通过replication协议模拟slave来获取源Redis节点数据并写入目标Redis从而实现数据同步的Redis同步工具
### 同步基本流程如下
![avatar](..//images/flow_path.png)


                          ┌──────┐                          ┌──────┐                 ┌──────┐
                          │      │                          │      │                 │      │
                          │matser│                          │syncer│                 │matser│
                          │      │                          │      │                 │      │
                          └───┬──┘                          └───┬──┘                 └───┬──┘
                              │                                 │                        │
                              │    PSYNC {replid} {offset}      │                        │   
                              │ ◀────────────────────────────── │                        │   
                              │                                 │                        │   
                              │  FULLRESYNC {replid} {offset}   │                        │   
                              │ ──────────────────────────────▶ │                        │   
                              │                                 │                        │   
                              │          binary data            │     send commands      │   
                              │  ----------------------------▶  │ ─────────────────────▶ │   
                              │                                 │                        │   
                              │       CONTINUE {replid}         │                        │   
                              │ ──────────────────────────────▶ │                        │   
                              │                                 │                        │   
                              │        backlog commands         │                        │   
                              │  ---------------------------─▶  │                        │   
                              │                                 │                        │   
                              │                                 │                        │   
                              ┼                                 ┼                        ┼   
#### 数据拉取流程如下
  * 1.建立socket
  * 2.发送auth user password (6.0新增user)
    
        OK 成功
        其他 error
    
  *  send->ping
     
         返回:
           ERR invalid password    密码错误
           NOAUTH Authentication required.没有发送密码
           operation not permitted 操作没权限
           PONG  密码成功
     
         作用：
            检测主从节点之间的网络是否可用。
            检查主从节点当前是否接受处理命令。
         
  * 发送从节点端口信息

        REPLCONF listening-port <port>
    
            -->OK 成功
            -->其他  失败
  * 发送从节点IP

        REPLCONF ip-address <IP>
    
             --> OK 成功
             --> 其他  失败

  * 发送EOF能力（capability）
    
        REPLCONF capa eof

           --> OK 成功
           --> 失败
        作用:
           是否支持EOF风格的RDB传输，用于无盘复制，就是能够解析出RDB文件的EOF流格式。用于无盘复制的方式中。 
           redis4.0支持两种能力 EOF 和 PSYNC2
           redis4.0之前版本仅支持EOF能力
    
  * 发送PSYNC2能力

         REPLCONF capa  PSYNC2
  
             --> OK 成功
             --> 失败
         作用: 
            告诉master支持PSYNC2命令 ,  master 会忽略它不支持的能力.  PSYNC2则表示支持Redis4.0最新的PSYN复制操作。

  * 发送PSYNC
    
        PSYNC {replid} {offset}
  
          -->  FULLRESYNC  {replid}  {offset}   完整同步
          -->  CONTINUE 部分同步
          -->  -ERR 主服务器低于2.8,不支持psync,从服务器需要发送sync
          -->  NOMASTERLINK  重试
          -->  LOADING       重试
          -->  超过重试机制阈值宕掉任务

        读取PSYNC命令状态，判断是部分同步还是完整同步

全量完成后：
  * PSYNC  ---> 启动heartbeat     
    
        REPLCONF ACK <replication_offset>
        心跳检测
          在命令传播阶段，从服务器默认会以每秒一次的频率
          发送REPLCONF ACK命令对于主从服务器有三个作用：
        作用:
          检测主从服务器的网络连接状态；
          辅助实现min-slaves选项；
          检测命令丢失。
    
        REPLCONF  GETACK 
          ->REPLCONF ACK <replication_offset>



RedisSyncer内部有断点续传、数据补偿、断线重连等机制来保证数据同步过程中稳定性和可用性,具体的机制如下。


#### 断点续传机制
RedisSyncer的断点续传机制是基于Redis的replid和offset来实现的，RedisSyncer有两个版本的断点续传机制v1和v2。


* v1版本:
  
        v1版本数据写入到目的端redis后，将offset持久化到本地，这样下次重启就从上次的offset拉取。但是由于该方案写目的端的操作和offset持久化不是一个原子的操作。如果中间发生中断会导致数据的不一致。
        例如，先写入数据到目的端成功，后持久化offset还没成功就发生了宕机、重启等情况，那么再次断点续传拉取上一次的offset数据最后就不一致了。
        

                      ┌─────────────────┐                                                                    ┌─────────────────┐
                      │                 │                          ┌─────────────────┐                       │                 │
                      │                 │ psync {replid} {offset}  │                 │                       │                 │
                      │                 │ ◀─────────────────────── │                 │       data            │                 │
                      │      Redis      │                          │   RedisSyncer   │─────────────────────▶ │      Redis      │
                      │                 │ ───────────────────────▶ │                 │                       │                 │
                      │                 │          data            │                 │                       │                 │
                      │                 │                          └─────────────────┘                       │                 │
                      └─────────────────┘                                   │                                └─────────────────┘
                                                                            │ write                                             
                                                                            │                                                    
                                                                            ▼                                                   
                                                                   ┌────────────────┐                                           
                                                                   │                │                                           
                                                                   │   checkpoint   │                                           
                                                                   │                │                                           
                                                                   └────────────────┘        

* v2版本:
  
        在v2版本策略中RedisSyncer会将每一个pipeline批次中不存在事务的的命令通过multi和exec进行包装,并在事务尾部插入offset检查点。
        当断点续传时需要从目标Redis的所以db库中查找checkpoint并找到所对应源节点当最大offset，再根据该offset进行断点续传。目前v2版本只支持目标为单机Redis的情况。
        在v2版本中
                      ┌─────────────┐                            ┌───────────────┐                     ┌─────────────┐
                      │             │  1.psync {replid} {offset} │               │                     │             │
                      │             │ ◀───────────────────────── │               │   4.checkpoint      │             │
                      │             │                            │               │ ──────────────────▶ │             │
                      │    Redis    │                            │  RedisSyncer  │     3.data          │    Redis    │
                      │             │ ─────────────────────────▶ │               │                     │             │
                      │             │    2.backlog commands      │               │                     │             │
                      └─────────────┘                            └───────────────┘                     └─────────────┘
*  v2命令事务封装结构


         ┌───────────────────────────────────────────────────────────────┐
         │                                                               │
         │                             multi                             │
         │                              ...                              │
         │                           commands                            │
         │                              ...                              │
         │      hset redis-syncer-checkpoint {ip}:{port}-version...      │
         │                             exec                              │
         │                                                               │
         └───────────────────────────────────────────────────────────────┘


v2 checkpoint检查点结构：
  
        HASH  hset redis-syncer-checkpoint {value}
        {value}:
            * {ip}:{port}-runid     {replid}
            * {ip}:{port}-offset    {offset}
            * pointcheckVersion     {version}

在Redis的事务机制中虽然不支持回滚，并且如果事务中间命令执行出错后但是事务还是被执行完成，但是除特殊情况外能够保证一致性。
在v2的机制中，为了防止'写放大'会在目标redis的每一个逻辑库中写入一个checkpoint，因此在执行断点续传操作的时候，同步工具会先扫描目标各个逻辑库中的checkpoint并选出里面最大offset的checkpoint作为断点续传的参数。
![avatar](../images/write/dataC.png)
####  数据补偿机制
  数据补偿机制是RedisSyncer同步任务写入数据失败时的补偿机制。数据补偿的前提是命令写入的幂等性，因此在RedisSyncer中会先将INCR、INCRBY、INCRBYFLOAT、APPEND、DECR、DECRBY等部分非幂等命令转换成幂等命令后再写入目标端Redis。
  RedisSyncer在目标为单机Redis或者Proxy的时候是通过pipeline机制将数据写入到目标Redis中的，每一个批次的pipeline的提交会返回一个结果列表， 同步工具会验证pipeline中结果的正确性，如果部分命令写入失败，同步工具对该批次与该key相关的命令进行重试。
  如果重试超过指定的阀值,将会宕掉任务。对于存在大key的list等非幂等结构，将不会进行数据补偿，直接宕掉任务。
#### 断线重连机制
  由于网络抖动等原因可能会导致同步工具源端与目标端连接在同步过程中断开,因此需要断线重试机制来保证在任务同步的过程中如果出现异常断开的问题。断线重连机制存在于与源Redis节点和RedisSyncer、RedisSyncer与目标Redis节点的连接之间，两者分别有各自的处理机制。
  ![avatar](../images/write/retry.png)

  * 源Redis与RedisSyncer的断线重连机制是通过记录的offset来实现的，当因网络异常等原因断开了连接时，RedisSyncer会重新尝试与源Redis节点建立连接，并通过当前任务记录的runid、offset等信息去拉取断开之前的增量数据，连接重新建立成功后RedisSyncer的同步任务将会无感知继续同步。 当断线重连超过指定重试阀值或者因为offset刷过导致没有办法续传数据时，RedisSyncer会宕掉当前当同步任务，等待人工干预。

  ![avatar](../images/write/retry2.png)

  * RedisSyncer与目标Redis之间的断线重连机制是通过缓存上一批次的pipeline的命令来实现的，当连接断开异常时RedisSyncer进行重重连回放上一批次写入失败的命令。当回放失败或者超过连续重试次数RedisSyncer会宕掉当前当同步任务，等待人工干预。
    
  ![avatar](../images/write/retry3.png)

#### 命令的链式处理
RedisSyncer中采用链式策略处理同步数据，任何一个策略返回失败，该key都将不会被同步。链式策略流程如图所示

  ![avatar](../images/write/retry4.png)


  每一个key在RedisSyncer都会经过一个策略链进行处理，只要有一个策略未通过则这个key将不会同步到目标Redis，比如key过期时间的计算策略如果计算出全量阶段key已过期，则将会自动抛弃该key。
  
  
  
#### 命令的链式处理

* 命令过滤
  
* key过滤
    
#### 多任务管理

* 任务启动流程
  * 任务启动前校验流程
  ![avatar](../images/write/startTask.png)
* 任务停止及清理流程
    
* 任务状态监控
* 任务异常及处理机制

#### rdb跨版本同步实现
    rdb文件存在向前兼容问题，即高版本的rdb文件无法导入低rdb版本的Redis

* 跨版本迁移实现机制
    1. REDIS跨版本间存在的问题：由于REDIS是向下兼容(低版本无法兼容高版本RDB)，在其RDB文件协议中存在一个vesion版本号标识,REDIS在RDB导入或者全量同步执行<font color=red size=2>rdbLoad</font>时会先检测<font color=red size=2>RDB VERSION</font>是否符合向下兼容，如果不符合则会抛出 <font color=red size=2>Can’t handle RDB format version</font>   错误。
    2. syncer跨版本实现机制
       对于全量同步RDB数据部分syncer将其分命令为两类进行处理

        1. 对于可能存在大key的结构比如：SET,ZSET,LIST,HASH等结构:
            1. 对于对数据成员没有顺序性要求的命令如：SET,ZSET,HASH命令解析器将其解析成一个或多个sadd,zadd,hmset等命令进行处理
            2. 对于对数据成员有顺序性要求的命令如:List等命令，若被命令解析器判断为大key并将其拆分为多个子命令，此时必须保证按顺序发送至目标REDIS节点
        2. 对于其他命令如：String等结构：
            1. 为保证其命令幂等性，命令解析器会根据目标REDIS节点的RDB版本进行序列化(实现<font color=red size=2>DUMP</font>)，传输模块会使用<font color=red size=2>REPLACE</font>反序列化到目标节点。（其中在redis3.0以下版本<font color=red size=2>REPLACE</font>命令不支持[<font color=red size=2>REPLACE</font>]）

RDB文件协议中关于 RDB VERSION部分
 
           #REDIS RDB文件结构开头部分示例
            ----------------------------# RDB is a binary format. There are no new lines or spaces in the file.
            52 45 44 49 53              # Magic String "REDIS"
            30 30 30 37                 # 4 digit ASCCII RDB Version Number. In this case, version = "0007" = 7   RDB VERSION字段
            ----------------------------
            FE 00                       # FE = code that indicates database selector. db number = 00

  关于 RDB VERSION检查部分伪代码

            def rdbLoad(filename):
                rio =  rioInitWithFile(filename);
                # 设置标记：
                # a. 服务器状态：rdb_loading = 1
                # b. 载入时间：loading_start_time = now_time
                # c. 载入大小：loading_total_bytes = filename.size
                startLoading(rio)
                # 1.检查该文件是否为RDB文件（即文件开头前5个字符是否为"REDIS"）
                if !checkRDBHeader(rio):
                    redislog("error, Wrong signature trying to load DB from file") 
                    return
                # 2.检查当前RDB文件版本是否兼容（向下兼容）
                if !checkRDBVersion(rio): 
                    redislog("error, Can't handle RDB format version")
                    return
             .........

                //Redis中关于RDB_VERSION检查的代码
                rdbver = atoi(buf+5);
                if (rdbver < 1 || rdbver > RDB_VERSION) {
                    rdbCheckError("Can't handle RDB format version %d",rdbver);
                    goto err;
                }
     

#### aof mix导入机制