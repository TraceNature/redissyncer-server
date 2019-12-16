# Quick Start Guid

# 构建与启动

* build jar file
```shell script
mvn clean install -pl syncer-webapp -am
```
* start server
```shell script
java -jar syncerplus-webapp-1.0.jar
```
* start in background
```shell script
nohup java -jar syncerplus-webapp-1.0.jar &
```    
相关启动参数配置请参阅[配置详情](serverconfig.md)

# 如何使用
redissyncer为多任务服务端程序,用户通过curl请求方式实现下列操作步骤
### 使用基本步骤
* 创建同步任务
* 启动同步任务
* 查看同步任务状态
* 停止任务
* 删除任务

### redis单实例同步实例
假设redissyncer所在服务器ip地址为10.0.0.100:8080;源redis地址192.168.0.10;目标redis地址192.168.0.20

* 创建同步任务

```shell script
curl -X POST \
  http://114.67.81.232:8080/api/v1/creattask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "redistest0102",
    "sourceRedisAddress": "114.67.100.239:6379;",
    "targetRedisAddress": "114.67.100.240:6379",
    "targetPassword": "redistest0102",
    "targetRedisVersion": 4,
    "taskName": "firsttest"
}'
```
返回值
```shell script
{
    "msg": "Task created successfully",
    "code": "2000",
    "data": {
        "taskids": [
            "10F7B3A0E5344598BAA9F847ADBFF9D6"
        ]
    }
}
```

返回值显示创建任务的"taskids",该id为任务唯一id,便于查询和删除任务时定位;当源库为集群时,任务会依据集群节点数被拆成若干任务,每个任务一个id

* 查看任务状态
```shell script
curl -X POST \
  http://114.67.81.232:8080/api/v1/listtasks \
  -H 'Content-Type: application/json' \
  -d '{
    "regulation": "bynames",
    "tasknames": [
        "firsttest"
    ]
}'
```

* 启动任务
```shell script
curl -X POST \
  http://114.67.81.232:8080/api/v1/starttask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskid": "10F7B3A0E5344598BAA9F847ADBFF9D6"
}'
```
任务启动时"taskid"为必填参数每次仅支持单任务启动

* 停止任务
```shell script
curl -X POST \
  http://114.67.81.232:8080/api/v1/stoptask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskids": [
        "10F7B3A0E5344598BAA9F847ADBFF9D6"
    ]
}'
```
停止任务支持多任务停止，
```json
"taskids": ["id1","id2" ..."idn"]
```

* 删除任务
```shell script
curl -X POST \
  http://114.67.81.232:8080/api/v1/removetask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskids": [
        "10F7B3A0E5344598BAA9F847ADBFF9D6"
    ]
}'
```
删除任务状态能为"RUN"