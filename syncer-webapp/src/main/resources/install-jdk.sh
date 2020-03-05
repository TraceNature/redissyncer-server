
#!/bin/bash
#
# @file
#   install-jdk.sh
#
# @date
#   2013-12-19
#
# @author
#   cheungmine
#
# @version
#   0.0.1pre
#
# @usage:
#   ./install-jdk.sh 192.168.122.206
#
################################################################################
. common.sh

#***********************************************************
# install_jdk
#   install jdk on machine: /usr/local/lib
#
# Parameters:
#   machine - root@ipaddr
#   jdkUri  - uri for fetching tarball
#
# Example:
#
#   install_jdk root@192.168.122.206 ftp://vm-ftp/pub/tarball/jdk-7u67-linux-x64.tar.gz
#
#***********************************************************
. common.sh

# YOU MIGHT CHANGE BELOW LINE TO GET YOUR JDK TARBALL:
DEFAULT_JDK_SRC="ftp://vm-ftp/pub/tarball/jdk-7u67-linux-x64.tar.gz"

# DO NOT CHANGE BELOW TWO LINES:
INSTALL_DIR="/usr/local/lib/java"
LOCAL_DIR="./.tmp"

function install_jdk() {
    echo -e "<INFO> install jdk on machine: $1"

    local DEST_LOGIN=$1
    local JDK_URI=$2
    local TAR=$(basename $JDK_URI)

    echo -e "<INFO> jdk: '$JDK_URI'"

    wget -c $JDK_URI -P $LOCAL_DIR -O $LOCAL_DIR/$TAR

    $(is_empty_dir "$LOCAL_DIR/jdk_untar")
    local ret=$

    case $ret in
    $DIR_NOT_EXISTED)
        mkdir -p $LOCAL_DIR/jdk_untar
        ;;
    $DIR_IS_EMPTY)
        ;;
    $DIR_NOT_EMPTY)
        rm -rf $LOCAL_DIR/jdk_untar/*
        ;;
    *)
        exit $ERR_FATAL_ERROR
        ;;
    esac

    # untar to jdk_untar
    tar -zxf $LOCAL_DIR/$TAR -C $LOCAL_DIR/jdk_untar

    $(is_empty_dir "$LOCAL_DIR/jdk_untar")
    local ret=$

    if [ "$ret" -eq "$DIR_NOT_EMPTY" ]; then
        local jdk_home=`ls $LOCAL_DIR/jdk_untar 2>/dev/null`
        echo $jdk_home
    else
        exit $ERR_FATAL_ERROR
    fi

    echo -e "<INFO> create folder on: $DEST_LOGIN:$INSTALL_DIR"
    local ret=`ssh $DEST_LOGIN "mkdir $INSTALL_DIR"`

    echo -e "<INFO> copy $jdk_home/ to: $DEST_LOGIN:$INSTALL_DIR/"
    local ret=`scp -r $LOCAL_DIR/jdk_untar/$jdk_home $DEST_LOGIN:$INSTALL_DIR`

    # remove local tar
    rm -rf $LOCAL_DIR/jdk_untar

    local DEST_JAVA_HOME=$INSTALL_DIR/$jdk_home

    echo -e "<TODO> remove old settings for install_jdk in /etc/profile"

    echo -e "<INFO> set /etc/profile: JAVA_HOME=$DEST_JAVA_HOME"
    local ret=`ssh $DEST_LOGIN "echo '' >> /etc/profile"`
    local ret=`ssh $DEST_LOGIN "echo '#!{{install_jdk@hgdb.net==>' >> /etc/profile"`

    local ret=`ssh $DEST_LOGIN "echo 'export JAVA_HOME=$DEST_JAVA_HOME' >> /etc/profile"`
    local ret=`ssh $DEST_LOGIN "echo 'export CLASSPATH=.:\\$JAVA_HOME/lib/tools.jar:\\$JAVA_HOME/lib/dt.jar' >> /etc/profile"`
    local ret=`ssh $DEST_LOGIN "echo 'export PATH=\\$JAVA_HOME/bin:\\$JAVA_HOME/jre/bin:\\$PATH' >> /etc/profile"`

    local ret=`ssh $DEST_LOGIN "echo '#!<==install_jdk@hgdb.net}}'>> /etc/profile"`

    local ret=`ssh $DEST_LOGIN ". /etc/profile"`
}


function uninstall_jdk() {
    echo -e "<TODO> uninstall jdk from: $1"
}

#=======================================================================
# ---- main() ----
if [ -n $1 ]; then
    DEST_IP=$1
    JDK_SRC=$DEFAULT_JDK_SRC

    if [ $# == 2 ]; then
        JDK_SRC=$2
    fi

    echo -e "<INFO> install jdk on '$DEST_IP', jdk: '$JDK_SRC'"

    install_jdk "root@$DEST_IP" "$JDK_SRC"
fi