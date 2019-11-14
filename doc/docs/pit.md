###redis增量同步中Incr和Decr自增自减命令可能由于网络抖动导致重试产生数据不一致性问题
    解决方案（未验证）：在写入target redis之前先查询所属key所存的value（incr/decr命令底层数据结构为string），若执行失败或超时
                        直接内存计算新值替换原值（incr和decr放入队列顺序消费）