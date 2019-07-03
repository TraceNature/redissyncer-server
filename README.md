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

/sync/startSync?sourceUri=redis://114.67.81.232?authPassword=redistest0102&targetUri=redis://114.67.81.232?authPassword=redistest0102&threadName=threadName01

### 三、使用方法


    
    启动同步任务：
    /sync/startSync?sourceUri=${sourceRedisUri}&targetUri=${targetRedisUri}&threadName=${threadName}
 
    参数：
    
        源redis连接地址
        ${sourceRedisUri}：redis://127.0.0.1:6319?authPassword=123456
        
        目标redis链接地址 
        ${targetRedisUri}  ： redis://127.0.0.1:6480?authPassword=123456
        
        任务名称
        ${threadName}：AtoB
    
    正在运行同步任务线程列表   
    /sync/listAlive
    
    已结束同步任务线程列表
    /sync/listDead
    
    根据任务名称关闭同步任务
    /sync/closeSync?name=${threadName}
    参数：
        任务名称
        ${threadName}  ： AtoB