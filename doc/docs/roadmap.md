# RoadMap

* 1.X

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

* 2.X

- [x] 同步模式拆分：全量、增量和全量+增量，增量offset包括beginbuf、endbuf
- [x]  断点续传：通过targetoffset续传，如果offset值已过期，该任务作废
- [x] 离线rdb或aof文件加载
- [x] cli客户端程序(完成基本架构及部分功能)


* 3.X
  
- [ ] key过滤    
- [ ] 命令过滤
- [ ] 限制任务数，根据内存容量限制创建任务
- [ ] 内存级别双向同步
- [ ] 集成log4j2,日志可通过application.yml或启动参数配置，默认输出位置 ./log
- [ ] 数据校验，由goclient集成
- [ ]  incr 、incrby等命令幂等操作
- [ ]  swagger 补充api说明
- [ ] goclient 适应v2 api
- [ ] goclient 实现交互模式类似redis-cli
* testcase完善，形成完整回归测试案例
   - [x] single2single
   - [ ] single2single with dbmap
   - [x] single2single 断点续传
   - [x] single2cluster
   - [x]  group2cluster
   - [ ]  group2cluster 断点续传
   - [ ] rdb导入
   - [ ] aof导入


* 4.X
- [ ] 兼容redis协议的其他kv产品例如Tides，TiKV
- [ ] 理解Tikv机制
- [ ] 实现Redis=>tikv全量+增量
- [ ] 实现Tikv数据全量rollback to redis
- [ ] tikv 作为redis持久化的完整方案 