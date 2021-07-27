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
  
- [x] key过滤 
- [x] 命令过滤
- [x] 限制任务数，根据内存容量限制创建任务
- [x] 实现增量续传2.0,通过redis事务命令，尽最大可能保证数据一致性
- [x] 目标连接retry机制
- [x] source.type target.type
- [x] 目标为kafka,实现命令订阅
- [x] 集成log4j2,日志可通过application.yml或启动参数配置，默认输出位置 ./log
- [x]  incr 、incrby等命令幂等操作
- [x]  swagger 补充api说明
- [x] goclient 适应v2 api
- [x] goclient 实现交互模式类似redis-cli
- [ ] 源端节点scan模式,使用scan命令实现不支持sync命令云的Redis的全量数据拉取
- [ ] 数据校验，由goclient集成
- [ ] 支持目标Redis sentinel模式
- [ ] 支持源Redis主从故障转移以及支持sentinel模式
- [x] 实现 rewrite
- [ ] 内存级别双向同步  
- [ ] 目标为rediscluster 实现pipeline写入


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
- [x] 任务元数据改为ETCD存储
- [ ] 实现portal及任务调度
- [ ] 实现任务在集群某节点不可用的情况下自动迁移至其他节点并续传任务
- [ ] 兼容redis协议的其他kv产品例如Tides，TiKV
- [ ] 理解Tikv机制
- [ ] 实现Redis=>tikv全量+增量
- [ ] 实现Tikv数据全量rollback to redis
- [ ] tikv 作为redis持久化的完整方案 
- [ ] 时间戳间隔自定义配置/自动获取
- [x] ttl时间偏差校准
