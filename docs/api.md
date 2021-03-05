### 创建任务接口

    http://10.0.0.90:8080/api/v1/creattask
    
    Method:POST
    
    请求头
    Content-Type:application/json
    
    请求体：
   
       
 * 字段描述
   
| field              | type               | example                                  | description                                                                                                                                                                                                                     | requred |
| ------------------ | ------------------ | ---------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| tasktype           | map<string,string> | "dbNum": {"1": "1"}                      | redis db映射关系，当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库                                                                                                  | false   |
| dbNum              | map<string,string> | "dbNum": {"1": "1"}                      | redis db映射关系，当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库                                                                                                  | false   |
| sourceRedisAddress | string             | "sourceRedisAddress": "10.0.0.1:6379"    | 源redis地址，cluster集群模式下地址由';'分割，如"10.0.0.1:6379;10.0.0.2:6379"                                                                                                                                                    | true    |
| sourcePassword     | string             | "sourcePassword": "sourcepasswd"         | 源redis密码默认值为""                                                                                                                                                                                                           | false   |  | false |
| targetRedisAddress | string             | "targetRedisAddress": "192.168.0.1:6379" | 目标redis地址 ,当目标redis为单实例或proxy时，填写单一地址即可，当目标redis为集群且需要借助jedis访问集群时地址用';'分割，"192.168.0.1:6379;192.168.0.3:6379;192.168.0.3:6379"                                                    | true    |
| targetPassword     | string             | "targetPassword": "xxx"                  | 目标redis密码 ，默认值为""                                                                                                                                                                                                      | false   |
| targetRedisVersion | string             | "targetversion":"4.0"                    | 目标redis版本, 该参数针对不可获取版本信息的情况，若可获取redis版本信息则按自动获取的版本信息进行处理                                                                                                                            | false   |
| taskName           | string             | "taskname":"product2test"                | 自定义任务名称                                                                                                                                                                                                                  | false   |
| autostart          | bool               | "autostart":true                         | 是否创建后自动启动，默认值false                                                                                                                                                                                                 | false   |
| afresh             | bool               | "afresh":true                            | 如果之前进行过全量同步并且offset值还在积压缓冲区时，为false时则从offset+1值开始进行增量同步，为true时则进行全量同步，缺省默认值为true (注：创建接口时 afresh字段仅和autostart为true时同时使用，afresh字段当startTask为必填字段) | false   |
| filterType         | string             | "filterType":"NONE"                      | 命令/key过滤类型   | false   |
| commandFilter      | string             | "commandFilter":"SET,DEL,FLUSHALL"       | 命令过滤器，不同命令间用,分割| false   |
| keyFilter          | string             | "keyFilter":"Redis(.*?)"                 | key名过滤器，需使用正则表达式| false   |

* filterType类型

| filterType类型         | type               |
|-----------------------|--------------------|
| NONE                  |  默认为NONE，过滤器不生效 |
| COMMAND_FILTER_ACCEPT | 只接受commandFilter中指定的命令，commandFilter中不同命令用,分割如[SET,DEL,FLUSHALL]，command大小写不敏感 |
| KEY_FILTER_ACCEPT     | 只接受keyFilter参数中key的数据（key大小写敏感），keyFilter需填写正则表达式如[Redis(.*?)] |
| COMMAND_AND_KEY_FILTER_ACCEPT  |  commandFilter和keyFilter同时生效 && 两者都满足放行 |
| COMMAND_OR_KEY_FILTER_ACCEPT   |  commandFilter和keyFilter指定的command 和key都接受 两者满足任意一者即生效放行 |
| COMMAND_FILTER_REFUSE          |  拒绝指定的commandFilter参数中指定的命令|
| KEY_FILTER_REFUSE              |  拒绝指定的keyFilter参数中指定的key|
| COMMAND_OR_KEY_FILTER_REFUSE   |  commandFilter和keyFilter指定的command 和key都接受 两者满足任意一者即拒绝|
| COMMAND_AND_KEY_FILTER_REFUSE  |  commandFilter和keyFilter同时生效 && 两者都满足拒绝|


      


### 错误码

| code | msg                                                           | data | description |
| ---- | ------------------------------------------------------------- | ---- | ----------- |
| 4000 | 源/目标redis连接失败                                          |      |             |
| 4001 | 任务URI信息有误，请检查                                       |      |             |
| 4002 | 相同配置任务已存在，请修改任务名                              |      |             |
| 4003 | 无法连接redis,请检查redis配置以及其可用性                     |      |
| 4024 | targetRedisVersion can not be empty /targetRedisVersion error |      |             |
| 4026 | dbMaping中库号超出Redis库的最大大小                           |      |             |
| 2000 | 成功                                                          |      |             |
| 400  | 错误请求（JSON格式错误）                                      |      |
| 500  | 服务端错误                                                    |      |
| 100  | 参数校验错误（如参数不为空之类的）                            |      |



##### 请求体事例：

    单机往单机迁移（如主从：推荐源redis节点使用从节点--目标redis节点使用目标主从的主节点）
    {
    	"sourcePassword": "xxxxxx",
    	"sourceRedisAddress": "10.0.0.100:6379",
    	"targetRedisAddress": "127.0.0.1:8002",
    	"targetPassword": "xxxxxx",
    	"taskName": "test",
    	"targetRedisVersion":2.8, 
    	"autostart":false,
    	"afresh":false
    }    
    
    多节点往单机迁移（如主从：推荐源redis节点使用从节点--目标redis节点使用目标主从的主节点）
    {
    	"sourcePassword": "xxxxxx",
    	"sourceRedisAddress": "10.0.0.100:6379;10.0.0.110:6379",
    	"targetRedisAddress": "127.0.0.1:6379;",
       	"targetPassword": "xxxxxx",
       	"taskName": "test",
       	"targetRedisVersion":2.8, 
       	"autostart":false
    }    
        

        cluster从多节点/单节点往集群迁移
        {
            "sourcePassword": "xxxxxx",
            "sourceRedisAddress": "10.0.0.100:6379;10.0.0.110:6379",
            "targetRedisAddress": "127.0.0.1:8002;127.0.0.1:8002;127.0.0.1:8002;127.0.0.1:8002;127.0.0.1:8002;127.0.0.1:8002",
            "targetPassword": "xxxxxx",
            "taskName": "test",
            "targetRedisVersion":2.8, 
            "autostart":false,
            "afresh":true
        }    
      
      返回值为：
      
      {
          "msg": "Task created successfully",
          "code": "200",
          "data": {
              "taskid": "89E601A6B23348BCB9B362C67BFB2926"
          }
      }
      

#### 启动任务接口 (只允许单任务)

    当创建任务接口参数 "autostart"设置为true时，创建完成任务会自动执行启动任务，当为flase时需使用本接口通过返回的taskid启动任务
    http://10.0.0.90:8080/api/v1/starttask
        
    Method:POST
    
    请求头
    Content-Type:application/json
    
    请求体：
    
    {
    	"taskid":"89E601A6B23348BCB9B362C67BFB2926",
    	"afresh":false
    }
    
### 状态码

| code | msg                                | data | description |
| ---- | ---------------------------------- | ---- | ----------- |
| 2000 | 启动成功                           |      |             |
| 4006 | taskid不能存在空值                 |      |             |
| 4007 | taskid为【taskId】的任务还未创建   |      |             |
| 4008 | 任务：【taskId】已经在运行中       |      |             |
| 2000 | 成功                               |      |             |
| 400  | 错误请求（JSON格式错误）           |      |
| 500  | 服务端错误                         |      |
| 100  | 参数校验错误（如参数不为空之类的） |      |
       
        
#### 停止任务接口
    停止正在处于运行的迁移同步任务
    http://10.0.0.90:8080/api/v1/stoptask
        
    Method:POST
    
    请求头
    Content-Type:application/json
    
    请求体：
    
    {
    	"taskids":["89E601A6B23348BCB9B362C67BFB2926"]
    }
    
### 状态码

| code | msg                                 | data | description |
| ---- | ----------------------------------- | ---- | ----------- |
| 2000 | 停止成功                            |      |             |
| 4006 | taskids中不能存在空值               |      |             |
| 4007 | 请先停止taskids中处于运行状态的任务 |      |             |
| 4020 | 不存在任务id为:taskId的任务         |      |             |
| 2000 | 成功                                |      |             |
| 400  | 错误请求（JSON格式错误）            |      |
| 500  | 服务端错误                          |      |
| 100  | 参数校验错误（如参数不为空之类的）  |      |
                 



### 编辑任务接口（* 3.0已废弃）

    http://10.0.0.90:8080/api/v1/edittask
    
    Method:POST
    
    请求头
    Content-Type:application/json
    
    请求体：
   
       
 * 字段描述
   
| field              | type               | example                                  | description                                                                                                                                                                  | requred |
| ------------------ | ------------------ | ---------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| dbNum              | map<string,string> | "dbNum": {"1": "1"}                      | redis db映射关系，当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况源与目标db一一对应,无该字段迁移源redis所有db库                                               | false   |
| sourceRedisAddress | string             | "sourceRedisAddress": "10.0.0.1:6379"    | 源redis地址，cluster集群模式下地址由';'分割，如"10.0.0.1:6379;10.0.0.2:6379"                                                                                                 | true    |
| sourcePassword     | string             | "sourcePassword": "sourcepasswd"         | 源redis密码默认值为""                                                                                                                                                        | false   |  | false |
| targetRedisAddress | string             | "targetRedisAddress": "192.168.0.1:6379" | 目标redis地址 ,当目标redis为单实例或proxy时，填写单一地址即可，当目标redis为集群且需要借助jedis访问集群时地址用';'分割，"192.168.0.1:6379;192.168.0.3:6379;192.168.0.3:6379" | true    |
| targetPassword     | string             | "targetPassword": "xxx"                  | 目标redis密码 ，默认值为""                                                                                                                                                   | false   |
| targetRedisVersion | string             | "targetversion":"4.0"                    | 目标redis版本, 该参数针对不可获取版本信息的情况，若可获取redis版本信息则按自动获取的版本信息进行处理                                                                         | false   |
| taskName           | string             | "taskname":"product2test"                | 自定义任务名称                                                                                                                                                               | false   |


### 状态码

| code | msg                                | data | description |
| ---- | ---------------------------------- | ---- | ----------- |
| 2000 | 编辑成功                           |      |             |
| 4023 | 不能编辑正在运行中的任务           |      |             |
| 4020 | 任务不存在                         |      |             |
| 2000 | 成功                               |      |             |
| 400  | 错误请求（JSON格式错误）           |      |
| 500  | 服务端错误                         |      |
| 100  | 参数校验错误（如参数不为空之类的） |      |
            
 #### 删除任务接口
 
     删除处于非运行状态的迁移同步任务
     
     请求地址：http://10.0.0.90:8080/api/v1/deletetask
         
     Method:POST
     
     请求头
     Content-Type:application/json
     
     请求体：
     
     {
     	"taskids":["89E601A6B23348BCB9B362C67BFB2926"]
     }
     
### 状态码

| code | msg                                                              | data | description |
| ---- | ---------------------------------------------------------------- | ---- | ----------- |
| 2000 | 删除成功                                                         |      |             |
| 4006 | taskids中不能存在空值                                            |      |             |
| 4007 | 请先停止taskids中处于运行状态的任务(taskids中有正在运行中的错误) |      |             |
| 4020 | 不存在任务id为:taskId的任务                                      |      |
| 2000 | 成功                                                             |      |             |
| 400  | 错误请求（JSON格式错误）                                         |      |
| 500  | 服务端错误                                                       |      |
| 100  | 参数校验错误（如参数不为空之类的）                               |      |


 #### 查看所有任务接口
 
     删除处于非运行状态的迁移同步任务
     请求地址：http://10.0.0.90:8080/api/v1/listtasks
         
     Method:POST
     
     请求头
     Content-Type:application/json
     
 * 字段描述
  
| field      | type     | example                       | description                                                                  | requred                           |
| ---------- | -------- | ----------------------------- | ---------------------------------------------------------------------------- | --------------------------------- |
| regulation | string   | "regulation":"all"            | 任务查询规则all、bynames、byids、bystatus                                    | true                              |
| tasknames  | string[] | "tasknames":["name1","name2"] | byname查询时必填任务名                                                       | true when "regulation":"byname"   |
| taskids    | string[] | "taskids":["id1","id2"]       | byid查询时必填任务id                                                         | true when "regulation":"byid"     |
| taskstatus | string   | "taskstatus":"live"           | bystatus查询时必填任务状态live(正常运行)、stop(正常停止)、broken(非正常停止) | true when "regulation":"bystatus" |


    请求体事例：
      查询全部任务
    {
    	"regulation":"all"
    }
    
    
### 状态码
  
| code | msg                                | data | description |
| ---- | ---------------------------------- | ---- | ----------- |
| 2000 | 删除成功                           |      |             |
| 4009 | tasknames 不能有为空的参数         |      |             |
| 4021 | bynames 参数类型错误               |      |             |
| 4006 | taskids中不能存在空值              |      |
| 4010 | taskstatus 不能有为空              |      |
| 2000 | 成功                               |      |             |
| 400  | 错误请求（JSON格式错误）           |      |
| 500  | 服务端错误                         |      |
| 100  | 参数校验错误（如参数不为空之类的） |      |
