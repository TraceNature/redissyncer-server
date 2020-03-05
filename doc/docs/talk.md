# redissyncer 是个啥
* 简洁说法：redis同步工具
* 专业说法：可部署的redis CDC服务

# redissyncer 能干啥
* redis单实例同步
* redis原生集群同步
* 跨版本同步
* rdb/aof文件导入

# 我们解决的redis数据同步中的坑
* rdb版本差异
* big key
* 网络异常中断


### 其他开源产品的不足
* 目前流行的开源版本为命令行模式通过配置文件完成相关参数配置
* 任务的创建与停止不便控制
* 不易部署和集成
* 不支持多任务
* 任务监控和状态检查不方便

  
### 用户案例
* 180广告
单节点16G数据

* 汇桔网
  单节点12G数据最大value　３Ｇ

* 9377游戏
用户自行使用工具迁移,单实例居多

* 银联
总数据量80G，目标节点16
![](../img/迁移.jpg)


* 京东广告
  反刷单系统　　6节点64G数据

### RoadMap
   * 1.0 
   - [x] 多线程写入
   - [x] 数据源及目标校验
   - [x] 多任务模式
   - [x] 自动适配源及目标版本
   - [x] 跨版本同步:低版本=>高版本
   - [x] 原生集群同步
   - [x] 通过jedis计算hash槽，进行集群分发 
   - [x] 全功能pipeline写入
   - [x] 大KV的支持
   - [x] pipeline失败补偿流程
   - [x]  任务状态持久化
   - [x] 接口梳理
     - [x] createtask
     - [x] starttask
     - [x] stoptask
     - [x] listtasks
     - [x] deletetask  
   
   * 2.0
   - [x] 同步模式拆分：全量、增量和全量+增量，增量offset包括beginbuf、endbuf
   - [x]  断点续传：通过targetoffset续传，如果offset值已过期，该任务作废
   - [x] 离线rds或aof文件加载
   - [x] 数据校验
   - [x] cli客户端程序(完成基本架构及部分功能)

### 项目地址
http://git.jd.com/csddevelop/redissyncer-service