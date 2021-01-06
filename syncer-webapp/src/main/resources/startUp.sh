#!/usr/bin/env bash
APP_NAME="app.jar"

usage() {
     echo "Usage: sh startUp.sh [start|stop|restart|status]"
     exit 1
 }
#运行成功返回0
startAppCmd(){
    nohup java -jar -Xms512m -Xmx2048m  ${APP_NAME}  >/dev/null 2>&1&
    if [ $? = 0 ];then
     return 0
    else
     return 1
    fi
}

#检查程序是否在运行
is_exist(){
    pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}'`
     #如果不存在返回1，存在返回0
    if [ -z "${pid}" ]; then
    return 1
  else
   return 0
 fi
}

java_is_exist(){
    java -version
    if [ $? = 0 ];then
      return 0;
    else
     return 1;
    fi
}

#安装jdk
install_openjdk(){
    yum install java-1.8.0-openjdk*
   if [ $? = 0 ]
    then
      return 0
    else
     return 1
   fi
}

jdk_version(){
  result=`java -version 2>&1 | sed '1!d' | sed -e 's/"//g' -e 's/version//'`
  return $result
}

app_is_exist(){
     result=`ps -ef | grep "${APP_NAME}" | grep -v grep | wc -l`
     if [ "$result" -eq 1 ]
       then
         return 0
       else
         return 1
     fi
}

is_exist2(){
     result=`ps -ef | grep "${APP_NAME}" | grep -v grep | wc -l`
     if [ "$result" -eq 1 ]
       then
         echo "app is running"
       else
          startAppCmd
          if [ $? -eq 0 ]; then
           echo "app is start success"
          else
           echo "app is start fail"
          fi
     fi
}

#build(){
#
#}

#启动方法
start(){
   java_is_exist
   if [ $? -eq 0 ]
     then
       echo "jdk is installed"
       app_is_exist
       if [ $? -eq 0 ]
         then
           echo "app is running"
         else
           startAppCmd
           if [ $? -eq 0 ]
            then
             echo "app is start success"
            else
             echo "app is start fail"
           fi
       fi
     else
       echo "jdk is not installed"
       install_openjdk
       if [ $? -eq 0 ]
         then
           ehco "jdk install success"
           startAppCmd
           if [ $? -eq 0 ]
             then
               ehco "app is start success"
             else
               echo "app is start fail"
           fi
         else
           echo "jdk install fail"
       fi
   fi

   is_exist
   if [ $? -eq 0 ]; then
    echo "${APP_NAME} is already running. pid=${pid}"
   else
    nohup java -jar ${APP_NAME}  >robotcenter.out 2>&1 &
   fi
}

#停止方法
stop(){
     is_exist
     if [ $? -eq "0" ]; then
       kill -9 $pid
     else
      echo "${APP_NAME} is not running"
     fi
}

#输出运行状态
 status(){
    is_exist
    if [ $? -eq "0" ]; then
      echo "${APP_NAME} is running. Pid is ${pid}"
    else
      echo "${APP_NAME} is NOT running."
    fi
    java_is_exist
    if [ $? -eq "0" ]; then
      java_v=$(jdk_version)
      echo "jdk version : ${java_v}"
    else
      echo "jdk version : null"
    fi
}




#重启
restart(){
 stop
 sleep 5
 start
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
 "start")
start
;;
"stop")
stop
;;
"status")
status
;;
"restart")
restart
;;
*)
usage
;;
esac
