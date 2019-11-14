# redissyncer

### 开源产品的不足
* 目前流行的开源版本为命令行模式通过配置文件完成相关参数配置
* 任务的创建与停止不便控制
* 无服务，不易部署和集成
* 不支持多任务
* 任务监控和状态检查不方便
## 项目目标
* 基于java编写服务化的redis同步服务；
* 基于redis迁移服务开发Cui及Gui可视化界面方便使用；
* 覆盖单实例多版本、多实例多版本、全量+增量、增量等多个场景；
* 满足客户迁移同步、缓存预热、redis灾备等实际应用场景；
* 易于与现有云环境集成；
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
   - [ ] 同步模式拆分：全量、增量和全量+增量，增量offset包括beginbuf、endbuf
   - [ ]  断点续传：通过targetoffset续传，如果offset值已过期，该任务作废
   - [ ] 离线rds或aof文件加载
   - [ ] 数据校验
   - [ ] cli客户端程序(完成基本架构及部分功能)

### 项目地址
http://git.jd.com/csddevelop/redissyncer-service