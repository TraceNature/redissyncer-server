
# Inews系统redis迁移

## 迁移工具部署地址

114.67.235.93  root  6LKS26o3TlU00ce

116.196.103.177  192.168.16.62  root  6LKS26o3TlU00ce

## 目标库

uri：redis-dkcv852xqvtf-proxy-nlb.jvessel-open-hb.jdcloud.com
密码：X5o8lPKCCXgpxUB
端口： 6379

### 安装jdk

```bash
yum install -y  java-1.8.0-openjdk-devel.x86_64
```

## redissyncer服务

* 部署位置
/root/syncer

* 服务启动
  
```bash
cd /root/syncer
./redissyncer.sh start

```

* 服务状态

```bash
cd /root/syncer
./redissyncer.sh statuse
```

* 服务停止

```bash
cd /root/syncer
./redissyncer.sh stop
```

### 下载rdb备份文件

```bash
wget -c 'https://rdsbak-bj-v4.oss-cn-beijing.aliyuncs.com/custins16880369/hins9645351_data_20200306043525.rdb?OSSAccessKeyId=LTAI23hcUqqtkmRx&Expires=1583646986&Signature=5tBDLUo1stPYehxGQgfRl01NxCs%3D' -O hins9645351_data_20200306043525.rdb
```

### 创建任务

* curl 方式

```bash
curl --location --request POST 'http://114.67.235.93:8081/api/v1/file/createtask' \
--header 'Content-Type: application/json' \
--data '{
    "fileAddress": "/root/data/hins9645351_data_20200306043525.rdb",
    "targetRedisAddress": "redis-dkcv852xqvtf-proxy-nlb.jvessel-open-hb.jdcloud.com:6379",
    "targetPassword": "X5o8lPKCCXgpxUB",
    "taskName": "在线文件测试",
    "autostart": true,
    "fileType": "RDB",
    "batchSize":1500,
    "targetRedisVersion": 4.0
}'
```
