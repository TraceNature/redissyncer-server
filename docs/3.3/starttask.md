

### 启动任务接口 (只允许单任务)

    当创建任务接口参数 "autostart"设置为true时，创建完成任务会自动执行启动任务，当为flase时需使用本接口通过返回的taskid启动任务
    
    http://10.0.0.90:8080/api/v2/starttask
        
    Method:POST
    
    请求头
    Content-Type:application/json
    
    请求体：
    
    {
    	"taskid":"89E601A6B23348BCB9B362C67BFB2926",
    	"afresh":false
    }

| field  |  type  |                   example                   |                         description                          |
| :----: | :----: | :-----------------------------------------: | :----------------------------------------------------------: |
| taskid | string | "taskid":"89E601A6B23348BCB9B362C67BFB2926" |                            任务id                            |
| afresh |  bool  |               "afresh":false                | 是否断点续传,false为断点续传,默认为true，断点续传成功的前提为之前改任务必须进行过同步，并且已经进入增量同步阶段，offset必须已经被记录，否则将会启动失败 |



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

​       