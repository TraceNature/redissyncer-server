# 编译
```
cargo build
```

# 功能与使用方法
该客户端程序为redissync-serveic客户端程序用与创建、启停、监控redis同步任务，在使用本Cui之前请确保服务端程序正常运行

* config.yml文件用于描述服务器链接的基本配置，程序默认读取当前目录下的config.yml，也可自定义文件名称及路径
  ```
  server: "114.67.81.232:8080"
  ```


# 测试用例

* 创建任务
   ```
   target/debug/rscli --config /home/develop/rustproject/rscli/src/config.yml createtask  -s 114.67.100.238:6379  --sourcepasswd     redistest0102 -t 114.67.100.239:6379  --targetpasswd    redistest0102  -n abc
   ```
* 通过文件创建任务
   * createtask.json
   ```
   {
   "dbNum": {
       "1": "1"
   },
   "idleTimeRunsMillis": 100000,
   "maxPoolSize": 110,
   "maxWaitTime": 10000,
   "minPoolSize": 100,
   "pipeline": "off",
   "sourcePassword": "redistest0102",
   "sourceRedisAddress": "114.67.100.239:6379",
   "targetRedisAddress": "114.67.100.239:800114.67.100.239:8002;   114.67.100.238:8002;114.67.100.238:800114.67.100.240:8002;   114.67.100.240:8003",
   "targetPassword": "",
   "threadName": "test",
   "timeBetweenEvictionRunsMillis": 1000
   }
   ```
   * 执行命令
   ```
   target/debug/rscli --config /home/develop/rustproject/rscli/src/   config.yml createtask -e src/createtask.json
   ```
* 查看全部在途任务
    ```
    target/debug/rscli --config /home/develop/rustproject/rscli/src/    config.yml listtasks -a
    ```

* 通过任务名删除任务
   ```
   target/debug/rscli --config /home/develop/rustproject/rscli/src/    config.yml deletetasks -n abv
   ```
