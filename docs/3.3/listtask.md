

### 查看所有任务接口



     删除处于非运行状态的迁移同步任务
     请求地址：http://10.0.0.90:8080/api/v2/listtasks
         
     Method:POST
     
     请求头
     Content-Type:application/json

 * 字段描述

|   field    | type     | example                       | description                                                  | requred                                          |
| :--------: | -------- | ----------------------------- | ------------------------------------------------------------ | :----------------------------------------------- |
| regulation | string   | "regulation":"all"            | 任务查询规则all、bynames、byids、bystatus                    | true                                             |
| tasknames  | string[] | "tasknames":["name1","name2"] | byname查询时必填任务名                                       | true when "regulation":"byname"                  |
|  taskids   | string[] | "taskids":["id1","id2"]       | byid查询时必填任务id                                         | true when "regulation":"byid"                    |
| taskstatus | string   | "taskstatus":"RDBRUNING"      | bystatus查询时必填任务状态CREATING(创建中),CREATED(创建完成),STARTING(启动中),STOP(停止中),PAUSE(暂停),BROKEN(任务异常),RDBRUNING(rdb数据同步中),COMMANDRUNING(实时同步中),FINISH(任务完成状态) | true when "regulation":"bystatus"                |
|  groupIds  | string[] | "groupIds":["id1","id2"]      | bygroupIds查询时必填任务id                                   | 若taskids和groupIds同时传入，只会优先获取taskids |


    请求体事例：
      查询全部任务
    {
    	"regulation":"all"
    }




### 分页查询列表

     删除处于非运行状态的迁移同步任务
     请求地址：http://10.0.0.90:8080/api/v2/listtasksByPage
         
     Method:POST
     
     请求头
     Content-Type:application/json

 * 字段描述

|    field    |   type   |            example            |                         description                          | requred                                          |
| :---------: | :------: | :---------------------------: | :----------------------------------------------------------: | ------------------------------------------------ |
| regulation  |  string  |      "regulation":"all"       |          任务查询规则all、bynames、byids、bystatus           | true                                             |
|  tasknames  | string[] | "tasknames":["name1","name2"] |                    byname查询时必填任务名                    | true when "regulation":"byname"                  |
|   taskids   | string[] |    "taskids":["id1","id2"]    |                     byid查询时必填任务id                     | true when "regulation":"byid"                    |
| taskstatus  |  string  |   "taskstatus":"RDBRUNING"    | bystatus查询时必填任务状态CREATING(创建中),CREATED(创建完成),STARTING(启动中),STOP(停止中),PAUSE(暂停),BROKEN(任务异常),RDBRUNING(rdb数据同步中),COMMANDRUNING(实时同步中),FINISH(任务完成状态) | true when "regulation":"bystatus"                |
|  groupIds   | string[] |   "groupIds":["id1","id2"]    |                  bygroupIds查询时必填任务id                  | 若taskids和groupIds同时传入，只会优先获取taskids |
| currentPage |   int    |         currentPage:1         |                           当前页码                           |                                                  |
|  pageSize   |   int    |          pageSize:10          |                          每页数据量                          |                                                  |


    请求体事例：
      查询全部任务
    {
    	"regulation":"all",
    	"currentPage":1,
    	"pageSize":10
    }


​
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

