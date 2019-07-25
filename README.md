# resdis同步服务
## 一、目录结构
#### 项目模块
* redis-syncerplus   父模块
* syncerplus-common  基础模块
* syncerplus-service 业务模块
* syncerplus-webapp  webapp模块（RESTful接口）


## 二、模块详情

### 1.redis-syncerplus 父模块
 - redis-syncerplus为父模块，通用jar包可直接在其maven pom.xml中引入，其他子模块同时生效

### 1.syncerplus-common模块
 - syncerplus-common为基础模块，存放通用代码（线程池、dataSource、通用工具类等)
 - 其他模块依赖syncerplus-common模块
 
 
### 2.syncerplus-service模块
 - 依赖 syncerplus-common模块
 - 单机redis数据迁移同步

![大体流程图](http://git.jd.com/csddevelop/redissyncer-service/raw/master/doc/img/01.png)




### 3.syncerplus-webapp模块
 -  依赖 syncerplus-common模块 和 syncerplus-service模块
 -  打包模块
 -  该模块对外提供restful接口
 





### 三、打包方法
    项目主目录下执行：
    mvn clean install -pl syncerplus-webapp -am
    
    运行方法：
    nohup java -jar syncerplus-webapp-1.0.jar &
    或
    java -jar syncerplus-webapp-1.0.jar


    配置文件以及日志外置启动
    nohup java -jar syncerplus-webapp-1.0.jar  --logging.config=/usr/local/jdks/config/logback.xml  &

### 三、使用方法


    
    
    启动新同步任务请求(POST)：JSON格式 
    Content-Type:application/json;charset=utf-8;

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
    

        
## 
|  参数   | 含义  |     
|  ----  | ----  |
| sourceUri  | 源redis连接地址 |
| targetRedisUri  | 单元格 |
| sourceUri  | 源redis连接地址 |
| threadName  | 任务名称 |
| sourceUri  | 源redis连接地址 |
| minPoolSize  | redis池最小大小 |
| maxPoolSize  | redis池最大小 |
| maxWaitTime  | 超时时间 |
| idleTimeRunsMillis  | 回收空闲未使用时间连接 |
| pipeline  | 跨版本同步是否开启通道 |

|  参数   | 缺省  |     
|  ----  | ----  |
| minPoolSize  | 可缺省 |
| maxPoolSize  | 可缺省 |
| maxWaitTime  | 可缺省 |
| idleTimeRunsMillis  | 可缺省 |
| pipeline  | 可缺省（默认为开启） |

缺省时为默认配置


    
    获取正在运行同步任务线程列表(GET请求)
    /sync/listAlive
    
    获取已结束同步任务线程列表(GET请求)
    /sync/listDead
    
    根据任务名称关闭同步任务
    请求接口(DELETE请求)：
    /sync/closeSync/${threadName}
    参数：
        任务名称
        ${threadName}  ： AtoB
        
         /sync/closeSync/AtoB
         
         

连接池配置：提供两种配置，一种在请求参数中添加，另一种为配置文件默认配置

        syncerplus:
      redispool:
        #池中空闲链接回收线程执行间隔时间  例：每隔1000毫秒执行一次回收函数
        timeBetweenEvictionRunsMillis: 300000
        #池中空闲连接回收未使用的时间  例：1800000毫秒未使用则回收  默认值是30分钟。
        idleTimeRunsMillis: 1800000
        #最小池大小
        minPoolSize: 1
        #最大池大小
        maxPoolSize: 25
        #连接超时时间
        maxWaitTime: 2000


连接池选择：other.properties

    # selefpool  or commonpool   selefpool为纯手写连接池 commonpool 为基于阿帕奇common-pool工具类构造的自定义连接池
    redispool.type=commonpool
    