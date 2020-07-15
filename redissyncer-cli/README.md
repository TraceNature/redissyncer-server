# golient
redissyncer 的客户端cli工具，方便迁移任务操作。

## build
```shell script
go build -o rscli
```



## 功能与使用方法
该客户端程序为redissyncer客户端程序用与创建、启停、监控redis同步任务，在使用本Cui之前请确保服务端程序正常运行

* config.yml文件用于描述服务器链接的基本配置，程序默认读取当前目录下的config.yml，也可自定义文件名称及路径
  ```
  server: "http://10.0.0.100:8080"
  ```


## quick start

* 创建任务
   ```
   target/debug/rscli --config /home/develop/rustproject/rscli/src/config.yml createtask  -s 10.0.0.100:6379  --sourcepasswd     xxxxxx  -t 192.168.0.100:6379  --targetpasswd    xxxxxx  -n abc
   ```
* 通过文件创建任务
   * createtask.json
   ```
   {
   "dbNum": {
       "1": "1"
   },
	"sourcePassword": "xxxxxx",
	"sourceRedisAddress": "10.0.1.100:6379",
	"targetRedisAddress": "192.168.0.100:6379",
	"targetPassword": "xxxxxx",
	"targetRedisVersion": 4.0,
	"taskName": "testtask",
	"autostart": true,
	"afresh": true,
	"batchSize": 100
   }
   
   ```
详细配置参数详见[API文档](../doc/docs/api.md)


* 执行命令
   ```
   target/debug/rscli --config /home/develop/rustproject/rscli/src/config.yml createtask -e src/createtask.json
   ```
* 查看全部在途任务
    ```
    target/debug/rscli --config /home/develop/rustproject/rscli/src/config.yml listtasks -a
    ```

* 通过任务名删除任务
   ```
   target/debug/rscli --config /home/develop/rustproject/rscli/src/config.yml deletetasks -i taskidxxxx
   ```

## 命令及参数说明

|参数|说明|
|---|---|
|-c|指定配置文件位置，未指定情况下默认读取执行命令的当前目录|

* createtask

|参数|说明|
|---|---|
|-e|指定任务描述文件|

* listtasks

|参数|说明|
|---|---|
|-a|列出所有任务|
|-i|通过id查找任务，多个id用','分割|
|-n|通过任务名称查找任务ｈ，多个任务名用','分割|
|-s|通过任务状态查找，'running' 'broken' 'stop'|
|-a|列出所有任务|

* stoptask

|参数|说明|
|---|---|
|-i|要停止的任务id|

* removetask

|参数|说明|
|---|---|
|-i|要删除的任务id|