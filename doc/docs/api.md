 ## createtask
 * url
  ```
  api/v1/creattask
  ```
  
* 字段描述
   
| field              | type               | example                                  | description                                                                                                                                                                  | requred |
| ------------------ | ------------------ | ---------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------- |
| dbNum              | map<string,string> | "dbNum": {"1": "1"}                      | redis db映射关系，当由此描述时任务按对应关系同步，未列出db不同步 ;无该字段的情况想，源与目标db一一对应                                                                       | false   |
| sourceRedisAddress | string             | "sourceRedisAddress": "10.0.0.1:6379"    | 源redis地址，集群模式下地址由';'分割，如"10.0.0.1:6379;10.0.0.2:6379"                                                                                                        | true    |
| sourcePassword     | string             | "sourcePassword": "sourcepasswd"         | 源redis密码默认值为""                                                                                                                                                        | false   |                                                                      | false   |
| targetRedisAddress | string             | "targetRedisAddress": "192.168.0.1:6379" | 目标redis地址 ,当目标redis为单实例或proxy时，填写单一地址即可，当目标redis为集群且需要借助jedis访问集群时地址用';'分割，"192.168.0.1:6379;192.168.0.3:6379;192.168.0.3:6379" | true    |
| targetPassword     | string             | "targetPassword": "xxx"                  | 目标redis密码 ，默认值为""                                                                                                                                                   | false   |
| targetversion      | string             | "targetversion":"4.0"                    | 目标redis版本, 该参数针对不可获取版本信息的情况，若可获取redis版本信息则按自动获取的版本信息进行处理                                                                         | false   |
| taskname           | string             | "taskname":"product2test"                | 自定义任务名称                                                                                                                                                               | false   |
| autostart          | bool               | "autostart":true                         | 是否创建后自动启动，默认值false                                                                                                                                              | false   |

* 返回值描述
  
 | field | description |
 | ----- | ----------- |
 | code  |             |
 | msg   |             |

 ## starttask
  * url
  ```
  api/v1/starttask
  ```
  
 * 字段描述

 | field   | type     | example                    | description                                   | requred |
 | ------- | -------- | -------------------------- | --------------------------------------------- | ------- |
 | taskids | string[] | "taskids":["uuid","uuid2"] | taskid在创建任务成功时返回或通过listtasks查询 | true    |


 ## stoptask
  * url
  ```
  api/v1/stoptask
  ```
  
 * 字段描述

| field | type | example | description | requred |
| ----- | ---- | ------- | ----------- | ------- | 
 | taskids | string[] | "taskids":["uuid","uuid2"] | taskid在创建任务成功时返回或通过listtasks查询 | true    |

 ## listtasks
   * url
  ```
  api/v1/listtasks
  ```
  
 * 字段描述
  
| field | type | example | description |requred |
| ----- | ---- | ------- | ----------- | ------ | 
|regulation|string|"regulation":"all"|任务查询规则all、bynames、byids、bystatus|true|
|tasknames|string[]|"tasknames":["name1","name2"]|byname查询时必填任务名|true when "regulation":"byname" |
|taskids|string[]|"taskids":["id1","id2"]|byid查询时必填任务id|true when "regulation":"byid" |
|taskstatus|string|"taskstatus":"live"|bystatus查询时必填任务状态live(正常运行)、stop(正常停止)、broken(非正常停止)|true when "regulation":"bystatus" |

 ## deletetask 
* url
  ```
  api/v1/deletetask
  ```
  
 * 字段描述

| field | type | example | description | requred |
| ----- | ---- | ------- | ----------- | ------- | 
 | taskids | string[] | "taskids":["uuid","uuid2"] | taskid在创建任务成功时返回或通过listtasks查询 | true    |




