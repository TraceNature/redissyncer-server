
# 架构

## 基础模块

* redis-syncerplus 父模块

  * redis-syncerplus为父模块，通用jar包可直接在其maven pom.xml中引入，其他子模块同时生效

* syncerplus-common模块

  * syncerplus-common为基础模块，存放通用代码（线程池、dataSource、通用工具类等)
  * 其他模块依赖syncerplus-common模块
  
* syncerplus-service模块
  
  * 依赖 syncerplus-common模块和 syncerplus-redis模块
  * 单机redis数据迁移同步

* syncerplus-webapp模块
  
  * 依赖 syncerplus-common模块 和 syncerplus-service模块
  * 打包模块
  * 该模块对外提供restful接口

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

* 错误处理机制
  1. filter
  2. 

* key幂等操作
  * INCR、INCRBY、INCRBYFLOAT处理机制
  * DECR、DECRBY处理机制
  * HINCRBY、HINCRBYFLOAT
  * BLPOP、BRPOP、BRPOPLPUSH、LPOP、LPUSH、LPUSHX、LTRIM、RPOP、RPOPLPUSH、RPUSH、RPUSHX
  * ZINCRBY
* 断点续传
* 任务保护机制
* 大key处理机制


