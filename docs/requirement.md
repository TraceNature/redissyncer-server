###syncer任务注意事项

####在线同步
* #####源端
        1.需要redissyncer能够直接连接真实的源节点，或者proxy能够支持sync命令
        2.需要源节点支持sync/psync命令
        3.支持社区版2.8-6.0版本命ßß令
        4.只需将主从节点的其中一者作为源节点即可，否则会出现非幂等类型数据写入两次
        5.repl-ping-slave-period必须小于readTimeout（redissyncer默认60000ms）
* #####目标端
        1.目标端支持单机、cluster、proxy分片集群类型
        2.目标节点需要兼容源节点的命令(高版本至低版本时)，若部分源节点支持目标节点不支持，则会造成不支持命令写入失败
        3.在迁移过程中尽量避免目标端有其他客户端写入

####rdb/aof文件导入
        1.需要同步程序能访问到持久化数据文件
        2.导入时目标端无需重启
        3.目标端支持单机、cluster、proxy分片集群等异构类型

###IOEXCEPTION

    1.出现ioexception可能原因
        repl-ping-slave-period必须小于readTimeout（redissyncer默认60000ms）
        源节点内存不够无法进行bgsave
        offset刷过
