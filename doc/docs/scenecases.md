# 场景案例
## 原生集群迁移
原生集群迁移可以有两种方式。
* 方式一
在创建任务时sourceRedisAddress和targetRedisAddress填写集群列表,各个节点用';'作为分隔符，redissyncer会根据source节点数量自动生成对应任务
```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisAddress": "192.168.0.10:6379;192.168.0.20:6379;192.168.0.30:6379",
    "targetRedisAddress": "192.168.1.10:6379;192.168.1.20:6379;192.168.1.30:6379",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "clustersync"
}'
```
* 方式二可以根据源节点创建独立任务，每个任务的targetRedisAddress必须为集群全部节点，以便redissyncer根据节点计算key所在的目标节点
```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisAddress": "192.168.0.10:6379",
    "targetRedisAddress": "192.168.1.10:6379;192.168.1.20:6379;192.168.1.30:6379",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "node1"
}'
```

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisAddress": "192.168.0.20:6379",
    "targetRedisAddress": "192.168.1.10:6379;192.168.1.20:6379;192.168.1.30:6379",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "node2"
}'
```

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisAddress": "192.168.0.30:6379",
    "targetRedisAddress": "192.168.1.10:6379;192.168.1.20:6379;192.168.1.30:6379",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "node3"
}'
```

方式一可以配合autostart参数简化配置，但对于集群规模较大数据存量数据较多场景会造成redissyncer负载过重，有宕机风险。对于集群规模较大或节点存量数据较多的情况，我们推荐第二种方式。这样可以灵活配置和启动任务轮流完成各节点全量同步任务减轻redissyncer服务器负载。单机不能满足同步任务的情况下可以部署多个redissyncer服务器组团儿完成同步任务。


## 断点续传
主动暂停任务或由于异常终端业务后，重新启动任务时可以通过afresh参数来指定是否断点续传，该参数为false时，任务会从停止前最后一个offset开始任务，如果为true则从头开始任务,该参数默认值为true。如果从新启动任务时redis缓冲区被覆盖即offset已经被覆盖则报错，无法续传。对于全量+增量的场景只有在全量数据同步完成后，进入增量同步时断点续传才生效。

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/starttask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskid": "10F7B3A0E5344598BAA9F847ADBFF9D6"
    "afresh": false
}'
```

## rdb或aof文件导入