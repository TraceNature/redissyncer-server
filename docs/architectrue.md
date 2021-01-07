
# 架构

## 基础模块

* redissyncer 父模块

  * redissyncer为父模块，通用jar包可直接在其maven pom.xml中引入，其他子模块同时生效

* syncer-common模块

  * syncer-common为基础模块，存放通用代码（线程池、dataSource、通用工具类等)
  * 其他模块依赖syncer-common模块
  
* syncer-jedis模块

  * syncer-jedis为redis客户端模块，存放定制修改版jedis代码
  * 其他模块依赖syncer-common模块
  
* syncer-replication模块
  
  * 依赖 syncer-common模块和 syncer-jedis模块
  * redis协议实现层（replica主从复制协议、命令解析器、命令转换、状态管理等）
    
* syncer-transmission模块
  
  * 依赖 syncer-common模块、syncer-jedis模块、syncer-replication模块
  * redis数据写入及任务管理层（数据写入、数据补偿、非幂等/幂等转换、任务管理等）
  
* syncer-webapp模块
  
  * 依赖 syncer-common模块 和 syncer-transmission模块
  * 打包模块
  * 该模块对外提供任务管理restful接口

## 基本功能

* 任务分类
  * 全量任务
  * 增量任务
  * 文件导入任务
* 全量任务
  全量是指redis某一实例或某一集群某一时点上的全部数据同步到另一实例或集群的任务

* 增量任务
  指进入命令传播状态后实时数据同步任务

* 文件导入任务
  支持rdb或aof文件导入
  
* 断点续传
  

## 机制及原理
<!-- <font color="#4590a3" size="6px">文字</font> -->

* 跨版本迁移实现机制
    1. REDIS跨版本间存在的问题：由于REDIS是向下兼容(低版本无法兼容高版本RDB)，在其RDB文件协议中存在一个vesion版本号标识,REDIS在RDB导入或者全量同步执行<font color=red size=3>rdbLoad</font>时会先检测<font color=red size=3>RDB VERSION</font>是否符合向下兼容，如果不符合则会抛出 <font color=red size=3>Can’t handle RDB format version</font>   错误。
    2. syncer跨版本实现机制
      对于全量同步RDB数据部分syncer将其分命令为两类进行处理
        
        1. 对于可能存在大key的结构比如：SET,ZSET,LIST,HASH等结构:
           1. 对于对数据成员没有顺序性要求的命令如：SET,ZSET,HASH命令解析器将其解析成一个或多个sadd,zadd,hmset等命令进行处理
           2. 对于对数据成员有顺序性要求的命令如:List等命令，若被命令解析器判断为大key并将其拆分为多个子命令，此时必须保证按顺序发送至目标REDIS节点
        2. 对于其他命令如：String等结构：
           1. 为保证其命令幂等性，命令解析器会根据目标REDIS节点的RDB版本进行序列化(实现<font color=red size=3>DUMP</font>)，传输模块会使用<font color=red size=3>REPLACE</font>反序列化到目标节点。（其中在redis3.0以下版本<font color=red size=3>REPLACE</font>命令不支持[<font color=red size=3>REPLACE</font>]）
  
  <font color=red size=3>RDB文件协议中关于 RDB VERSION部分</font>

    ```
      
           #REDIS RDB文件结构开头部分示例
            ----------------------------# RDB is a binary format. There are no new lines or spaces in the file.
            52 45 44 49 53              # Magic String "REDIS"
            30 30 30 37                 # 4 digit ASCCII RDB Version Number. In this case, version = "0007" = 7   RDB VERSION字段
            ----------------------------
            FE 00                       # FE = code that indicates database selector. db number = 00

  ```
    <font color=red size=3>关于 RDB VERSION检查部分伪代码</font>
  ```


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
     ```
* 错误处理机制
  1. 命令写入错误处理机制
     1. 对于幂等命令以及已经非幂等转幂等的命令进行重试写入处理
     2. 对于部分无法转幂等的命令进行人工干预处理以保证数据最终一致性
  2. 全量阶段任务异常停止或断开处理
     1.由于全量阶段无法续传，因此宕掉任务处理
  3. 增量阶段任务错误处理
     1.由于每个任务会记录同步的offset,因此当任务错误后会从当前offset重试，当offset被刷过时自动宕掉任务
 

* key幂等操作
  * INCR、INCRBY、INCRBYFLOAT处理机制
  * DECR、DECRBY处理机制
  * HINCRBY、HINCRBYFLOAT
  * BLPOP、BRPOP、BRPOPLPUSH、LPOP、LPUSH、LPUSHX、LTRIM、RPOP、RPOPLPUSH、RPUSH、RPUSHX
  * ZINCRBY
* 断点续传
    在REDIS 2.8+版本支持PSYNC,而命令格式为：
    
        PSYNC <runid> <offset> 
        runid:主服务器ID
        offset:从服务器最后接收命令的偏移量
    因此每一个同步子任务同步程序都会记录其 runid和offset,一旦在增量过程中断开，同步程序会通过记录的runid和offset进行增量数据续传
   
* 任务保护机制
    当任务被主动触发时，同步程序会自动断开与源节点的连接，然后进入任务保护状态，当还处于同步程序内数据全部写入目标后，任务保护状态终止
* 大key处理机制
    对于list,set,zset,hash等结构，当大小超过设置的阈值时进行迭代命令切割,根据设置的batchSize进行设置切割大小
