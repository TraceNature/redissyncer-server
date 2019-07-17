
### 1. 环境描述 
 name | 原生Redis |  京东云Redis单实例
-|-|-
服务器配置 |京东云服务器2核8G | 4GB内存 |
 redis 版本 |  4.0.11          |  2.8.19 |


### 2. Redis配置 
name | 原生Redis |  京东云Redis单实例
-|-|-
maxmemory | 0（无限制） | 未知 |
maxmemory-policy | volatile-lru | volatile-lru |

### 3. 启动Redis数据迁移与同步任务 
#### 3.1）. 请求地址（POST）JSON格式 
    http://xxxxx.com/sync/startSync
#### 3.2）. 请求参数 
    （maxPoolSize请设置小于redis-server允许连接的客户端数量）
    请求头：
    Content-Type:application/json;charset=utf-8;
    请求体：
    {
        "sourceUri": "redis://127.0.0.1:6379?authPassword=123456",
        "targetUri": "redis://127.0.0.1:6380?authPassword=123456",
        "threadName": "test01"
    }
    或（携带连接池参数）
    {
        "sourceUri": "redis://127.0.0.1:6379?authPassword=123456",
        "targetUri": "redis://127.0.0.1:6380?authPassword=123456",
        "threadName": "test01",
        "idleTimeRunsMillis": 300000,
        "maxPoolSize": 20,
        "maxWaitTime": 10000,
        "minPoolSize": 1
    }



    上述参数缺省时为数application.yml文件默认配置参数    

    返回结果为以下参数下时为启动成功：
    {
        "msg": "success",
        "code": "200"
    }

### 4. 测试
    使用benchmark往原生Redis内插入大量数据测试迁移同步效果
    
    benchmark命令行：
    
    redis-benchmark -h ${host} -p ${port} -a ${password} -c 100  -r 10000000 -n 20000000 -t set,lpush -q -P 16
    
    redis-benchmark -h 114.67.81.232 -p 6379 -a redistest0102  -c 100 -r 10000000 -n 20000000 -t set,lpush -q -P 16
    
    -r 设置10000000随机key
    -c 模拟100个客户端
    -n 20000000次请求 
    Pipelining 16条命令
    
    SET: 20271.95 requests per second
    LPUSH: 18261.79 requests per second
    
    测试结果：
    原生
    # Keyspace
    db0:keys=8645616,expires=0,avg_ttl=0
    
    京东云Redis
    # Keyspace
    db0:keys=8645615,expires=0,avg_ttl=0