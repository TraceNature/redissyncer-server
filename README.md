# RedisSyncer
[English](README_en.md)

RedisSyncer是一个redis同步工具，应用于redis单实例及集群同步

## 项目目标

* 基于java编写服务化的redis同步服务；
* 基于redis迁移服务开发Cui及Gui可视化界面方便使用；
* 覆盖单实例多版本、多实例多版本、全量+增量、增量等多个场景；
* 满足客户迁移同步、缓存预热、redis灾备、集群扩缩容、redis版本升级等实际应用场景；
* 易于与现有云环境集成；

## Quick start

请参阅[Quick Start Guide](docs/quickstart.md),文档包括构建及部署方法及基本使用方法

## RoadMap

请参阅　[RoadMap](docs/roadmap.md)

## 编译环境
|     **环境条件** |   **版本号**  |  
|      :----:     |     :----:   | 
|  \[Maven\]     |  \[3.0+ \]   |  
|  \[JDK\]       |  \[1.8+ \]   |

## 运行环境

|     **环境条件**    |    **版本号**    |  
|       :----:       |    :----:       | 
|  \[JDK\]          |    \[1.8+ \]    |  

## 支持Redis版本
|     **环境条件**     |**版本号**  |  
| :----:| :----: |
|  \[Redis\]    |         \[2.8-6.0\]  |  

## 支持数据类型
|     **源数据类型**          |       **说明**             |
| :----:| :----: |
|  \[Redis\]                |         \[存量数据同步\]    |  
|  \[Redis\]                |         \[增量实时同步\]    |  
|  \[Redis\]                |     \[存量+增量实时同步\]    |  
|  \[Redis\]                |     \[生成实时增量AOF文件\]  |
|  \[本地RDB\]                |     \[本地RDB文件导入\]    |  
|  \[在线RDB\]                |     \[文件url导入\]       | 
|  \[本地AOF\]                |     \[本地AOF文件导入\]    | 
|  \[在线AOF\]                |     \[文件url导入\]       | 
|  \[本地混合文件\]            |     \[本地混合文件导入\]    | 
|  \[在线混合文件\]            |     \[文件url导入\]        | 

[comment]: <> (##支持命令)




[comment]: <> (|  命令  |  命令  | 命令    | 命令    |)

[comment]: <> (| :----:| :----: | :----: | :----: |)

[comment]: <> (| APPEND     | BLPOP      | SADD        |)

[comment]: <> (| SET        | BRPOP      | SCARD       |)

[comment]: <> (| SETEX      | BRPOPLPUSH | SDIFFSTORE  |)

[comment]: <> (| SETNX      | 	LINSERT   | SINTERSTORE |)

[comment]: <> (| GETSET     | 	LPOP      |   SMOVE     |)

[comment]: <> (| SETBIT     | LPUSH      |    SPOP     |)

[comment]: <> (| SETRANGE   | LPUSHX     |    SREM     |)

[comment]: <> (| 	MSET     | LREM       | SUNIONSTORE |)

[comment]: <> (| MSETNX     | LSET       | 单元格 |)

[comment]: <> (| PSETEX     | LTRIM      | 单元格 |)

[comment]: <> (| 	INCR     | RPOP       | 单元格 |)

[comment]: <> (| INCRBY     | RPOPLPUSH  | 单元格 |)

[comment]: <> (|INCRBYFLOAT | RPUSH      | 单元格 |)

[comment]: <> (|    DECR    | RPUSHX     | 单元格 |)

[comment]: <> (| DECRBY     | 单元格      | 单元格 |)

## 致谢
### Jedis
本项目Redis客户端采用[Jedis](https://github.com/redis/jedis)
### Replicatior
本项目数据拉取协议层基于[replicatior](https://github.com/leonchen83/redis-replicator) 二次开发