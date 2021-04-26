
## Version 3.4

#### 创建任务

##### 哨兵集群
    http://127.0.0.1:8081/api/v2/createtask
###### method
    POST   
    
    Content-Type   application/json
###### body
    {
        "sourceRedisAddress":"127.0.0.1:26379;127.0.0.1:26380;127.0.0.1:26381",
        "sourcePassword": "123456",          
        "sourceRedisType":"SENTINEL",
        "sourceRedisMasterName":"local-master",
        "sourceSentinelAuthPassword":"123456",
        "targetRedisAddress": "127.0.0.1:6379",
        "targetRedisType":"SINGLE",
        "targetRedisVersion": 5,
        "taskName": "Redis_SENTINEL_TEST",
        "autostart":true
    }
###### result
    {
      "code": "2000",
      "msg": "request is successful",
      "data": [
        {
            "code": "2000",
            "taskId": "CD5A7555AB614A4BAA2FD22EED12D401",
            "groupId": "CD5A7555AB614A4BAA2FD22EED12D401",      
            "msg": "Task created successfully and entered running state"
          }
        ]
    }

#### 单节点模式(每个节点一个任务)
    {
        "sourceRedisAddress":"114.67.76.82:16379",
        "sourcePassword": "123456",          
        "sourceRedisType":"SINGLE",
        "targetRedisAddress": "127.0.0.1:6379",
        "targetRedisType":"SINGLE",
        "targetRedisVersion": 5,
        "taskName": "Redis_SENTINEL_TEST",
        "autostart":true
    }

#### 目标Cluster模式

    {
        "sourceRedisAddress":"127.0.0.1:6379",
        "sourcePassword": "123456",          
        "sourceRedisType":"SINGLE",
        "targetRedisAddress": "127.0.0.1:16379;127.0.0.1:16380;127.0.0.1:16381",
        "targetRedisType":"clutser",
        "targetRedisVersion": 5,
        "taskName": "Redis_SENTINEL_TEST",
        "autostart":true
    }

#### 源端Cluster模式(每个节点一个任务，只需填主节点/一个从节点，以免造成数据重复写入，每一个节点作为一个任务，当作单机模式使用)
    {
        "sourceRedisAddress":"127.0.0.1:16379;127.0.0.1:16380;127.0.0.1:16381",
        "sourcePassword": "123456",          
        "sourceRedisType":"SINGLE",
        "targetRedisAddress": "127.0.0.1:6379",
        "targetRedisType":"SINGLE",
        "targetRedisVersion": 5,
        "taskName": "Redis_SENTINEL_TEST",
        "autostart":true
    }