#!/bin/bash

# config java
export JAVA_HOME=$HOME/openlogic-openjdk-8u262-b10-linux-64
export PATH=$JAVA_HOME/bin:$PATH:.
export CLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dt.jar:.

SHELL_FOLDER=$(dirname $(readlink -f "$0"))

jarpath=${SHELL_FOLDER}/syncer-webapp
jarname=redissyncer-server.jar

jarproc=`jps | grep ${jarname} `

procnum=`echo ${jarproc}|awk '{print $1}'`

help()
{ 
cat << EOF
Usage: $0 start|stop|status|restart
EOF
exit 0
}


start()
{
echo "starting ${jarname}"

if  [ "" != "${procnum}" ] ;then
    echo "${jarname} is running please stop it firest!"
    echo "${jarname} start failure!"
    exit 1
fi

java -Xms4096m -Xmx4096m -jar ${jarpath}/${jarname}  \
-XX:-UseGCOverheadLimit \
--logging.level.root=info \
--server.port=8080 \
 >> stdout.log &2>1 &

sleep 2
proc=`jps | grep ${jarname}|awk '{print $1}' `

if  [ "" = "${proc}" ] ;then
    echo "${jarname} start failure!"
else
    echo "${jarname} start success!"
    echo `jps -lvm|grep ${proc}`
fi

exit 0
}

status()
{

if  [ "" = "${procnum}" ] ;then
    echo "${jarname} not available!!"
else
    echo "${jarname} is running!"
    echo `jps -lvm|grep ${procnum}`
fi

}

stop()
{
if  [ "" = "${procnum}" ] ;then
    echo "${jarname} is already stopped!!"
    exit 0
fi

kill -15 ${procnum}
echo ${jarproc} be killed !
echo ${jarname} stopped !
}

restart()
{
stop
start
}

if [ -z "$1" ]; then
help
exit 0
fi


case $1 in
[hH][eE][lL][pP]) help ;;
-[hH]) help ;;
start) start ;;
status) status ;;
stop) stop ;;
restart) restart ;;
*)     help ;;
esac
