# 工程相关
- [x] swagger 补充api说明
- [ ] 返回值规范，例如状态可以分为taskdesc{},execstatus{}
- [ ] readme增加如何生成swagger文档部分
- [x] goclient 适应v2 api
- [x] goclient 实现交互模式类似redis-cli

# 测试相关

* testcase完善，形成完整回归测试案例
  - [x] single2single
  - [x] single2single with dbmap
  - [x] single2single 断点续传
  - [x] single2cluster
  - [x] cluster2cluster

# 功能完善
- [ ] 固化开发测试环境，形成列表
- [x] 做一个redis的docker-compose，配置环境
- [ ] 两套集群4.0和5.0
- [x] key丢弃机制,允许最大丢弃多少key后停止任务
- [x] 状态项新增任务停止原因
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
- [ ] 如何自动生成restful sdk

# 运营
一线服务团队纳入支持体系
建立vm标准镜像
找外包前端
前端测试puppeteer

# 开源准备工作

- [ ] 开源协议编写

* 代码梳理
  - [x] 删除用于调试的实验性代码
  - [x] 规范代码格式
  - [x] 添加模块注释
  - [x] 添加类及关键方法注释
  - [ ] 完善单元测试
  - [ ] 源代码添加协议头
  - [x] golang 开发工程添加makefile
  - [ ] 回归测试
  
* 文档梳理  
  - [x] 文档目录修改，将docs提至项目跟目录
  - [x] 用户文档完善
  - [x] 架构文档完善
  - [x] Api文档完善
  - [x] 增加英文文档
  
* 建立代码及文档规范
* 团队开发工作尊照github flow 运作(需要一段适应期)

