# 工程TODO
- [ ] swagger 补充api说明
- [ ] 返回值规范，例如状态可以分为taskdesc{},execstatus{}
- [ ] readme增加如何生成swagger文档部分
- [ ] goclient 适应v2 api
- [ ] goclient 实现交互模式类似redis-cli

* testcase完善，形成完整回归测试案例
   - [x] single2single
   - [ ] single2single with dbmap
   - [x] single2single 断点续传
   - [x] single2cluster
   - [x] cluster2cluster


- [ ] 固化开发测试环境，形成列表
- [ ] 做一个redis的docker-compose，配置环境
- [ ] 两套集群4.0和5.0
- [ ] key丢弃机制
       允许最大丢弃多少key后停止任务
- [ ] 状态项新增任务停止原因
- [x] 状态增加dataincomeinterval，最后数据流入时间，返回当前时间与最后流入数据时间的差值
- [ ] 如何根据jvm已用内存和xmx差值限制任务
- [x] 服务端日志改用log4j2
- [ ] 命令过滤
- [ ] key过滤
  


# 产品定位
- [ ] 产品定位
- [ ] 产品发展策略
- [ ] 如何与外部生态融合
- [ ] 如何直接变现

# 其他技术项
-  [] 如何自动生成restful sdk
