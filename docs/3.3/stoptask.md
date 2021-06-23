
### 停止任务接口
    停止正在处于运行的迁移同步任务
    http://10.0.0.90:8080/api/v2/stoptask
        
    Method:POST
    
    请求头
    Content-Type:application/json
    
    请求体：
    
    {
    	"taskids":["89E601A6B23348BCB9B362C67BFB2926"]
    }

|  field  |   type   |                    example                     | description | requred |
| :-----: | :------: | :--------------------------------------------: | :---------: | :-----: |
| taskids | string[] | "taskids":["89E601A6B23348BCB9B362C67BFB2926"] |   任务id    |  true   |



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

