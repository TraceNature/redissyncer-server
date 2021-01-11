# Quick Start Guid

[简体中文](quickstart.md)

## Build and Start

### Build jar package

* build jar file
  
```shell script
mvn clean package -Ddockerfile.skip
```

* start server

```shell script
java -jar syncer-webapp/syncer-webapp.jar
```

* start in background

```shell script
nohup java -jar syncer-webapp/syncer-webapp.jar &
```

### Build Docker Image

* build docker image

```shell
mvn clean package
```

* start container

```shell
docker run -idt -m 2G -p 8000:80 --name redissyncer redissyncer:v2.0.8
```

* docker-compose.yml

```yaml
# deploy local redissyncer service
version: "2.4"

services:
  redissyncer-server:
      image: jiashiwen/redissyncer:v2.0.8
      ports:
      - "8000:80"
      mem_limit: 2g
      environment:
        SPRING_ENV: "--server.port=80"
      volumes:
        - /etc/localtime:/etc/localtime:ro
        - ./log:/opt/redissyncer/log
      container_name: redissyncer-server

```

For related startup parameter configuration, please refer to[Configuration details](serverconfig.md)

## Usage

Redissyncer is a multitasking server program, and the user implements the following steps through curl request

### Steps for usage

* Create task
* Start task
* View task status
* Stop task
* Delete task

### Redis single instance synchronization example

Assume that the ip address of the server where redissyncer is located is 10.0.0.100:8080; the source redis address is 192.168.0.10; the target redis address is 192.168.0.20

* Create synchronization task

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/createtask \
  -H 'Content-Type: application/json' \
  -d '{
    "sourcePassword": "xxxxxx",
    "sourceRedisAddress": "192.168.0.10:6379;",
    "targetRedisAddress": "192.168.0.20:6379",
    "targetPassword": "xxxxxx",
    "targetRedisVersion": 4,
    "taskName": "firsttest"
}'
```

return value

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

The return value shows the "taskids" that created the task. The id is the unique id of the task, which is convenient for locating when querying and deleting the task; when the source database is a cluster, the task will be divided into several tasks according to the number of cluster nodes, and each task has an id

* View task status

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/listtasks \
  -H 'Content-Type: application/json' \
  -d '{
    "regulation": "bynames",
    "tasknames": [
        "firsttest"
    ]
}'
```

* Start task

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/starttask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskid": "10F7B3A0E5344598BAA9F847ADBFF9D6"
}'
```

任务启动时"taskid"为必填参数每次仅支持单任务启动
When the task is started, "taskid" is a required parameter and only supports single task start each time

* Stop task

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/stoptask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskids": [
        "10F7B3A0E5344598BAA9F847ADBFF9D6"
    ]
}'
```

```json
"taskids": ["id1","id2" ..."idn"]
```

Stop task support multi-task stop

* Remove Task

```shell script
curl -X POST \
  http://10.0.0.100:8080/api/v1/removetask \
  -H 'Content-Type: application/json' \
  -d '{
    "taskids": [
        "10F7B3A0E5344598BAA9F847ADBFF9D6"
    ]
}'
```

Only tasks with status "STOP" or "Broken" can be deleted

* API and detailed parameters please refer to [api](api.md)

### Use client

[Client User Manual](https://github.com/TraceNature/redissyncer-cli/blob/master/docs/quickstart.md)

### Precautions

* During migration and before business switching, please observe the log information repeatedly to confirm whether there is any abnormality. The log location is ~/log.
* Please fully check before business switching, especially the consistency of data.
* Redissyncer is recommended to be deployed on a separate idle machine to prevent insufficient resources, such as memory, bandwidth, and IOPS.
* Pay attention to whether the RDB transmission is overtime;
* For the slave item in redis client buf, set a sufficiently large buffer size and timeout period.
* Before starting the synchronization task, please confirm whether the redis source machine has enough memory to allow at least one RDB file to be generated.
  
