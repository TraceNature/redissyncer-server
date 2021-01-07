#!/bin/bash

SHELL_FOLDER=$(dirname $(readlink -f "$0"))

jarpath=${SHELL_FOLDER}
jarname=redisyncer-2.0.8.jar

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
--server.port=8998 \
 >> stdout.log &2>1 &

proc=`jps | grep ${jarname} `

sleep 2

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
