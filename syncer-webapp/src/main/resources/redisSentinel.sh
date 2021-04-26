#!/usr/bin/env bash

cd /Users/zhanenqiang/redis/redisSentinel/redis-6.0.10/src

echo "jdk version : $(pwd)"

./redis-server '/Users/zhanenqiang/redis/sentinel/server/redis-master.conf'
./redis-server '/Users/zhanenqiang/redis/sentinel/server/redis-slave1.conf'
./redis-server '/Users/zhanenqiang/redis/sentinel/server/redis-slave2.conf'

./redis-server '/Users/zhanenqiang/redis/sentinel/sentinel1/redis-sentinel-1.conf'
./redis-server '/Users/zhanenqiang/redis/sentinel/sentinel1/redis-sentinel-2.conf'
./redis-server '/Users/zhanenqiang/redis/sentinel/sentinel1/redis-sentinel-3.conf'

#./redis-server

pwd(){
  result=`pwd`
  return $result
}


startRedis(){
  path= ./redis-server `$1`

  echo $path
 }

startRedis '/Users/zhanenqiang/redis/redisSentinel/server/redis-master.conf'

startRedis '/Users/zhanenqiang/redis/redisSentinel/server/redis-slave1.conf'
startRedis '/Users/zhanenqiang/redis/redisSentinel/server/redis-slave2.conf'