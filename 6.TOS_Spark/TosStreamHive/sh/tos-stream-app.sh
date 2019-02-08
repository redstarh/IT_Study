#!/bin/sh

#------------------------------------------------------------------------------
# Control Script for the TosStreamApp
#
# Environment Variable Prerequisites
#
# HDP_HOME          (Optional)   default /usr/hdp/2.6.5.0-292
# SPARK_HOME        (Optional)   default /usr/hdp/2.6.5.0-292/spark
# TOS_HOME          (Mendatory)  tos home  ex) /tos/offering_m/tos
# APP_HOME          (Mendatory)  app home  ex) /tos/offering_m/tos/TosStreamApp
# APP_CLASS         (Mendatory)  app class ex) com.sktelecom.tos.stream.app.StreamApp
# APP_NAME          (Mendatory)  app name  ex) TosStreamApp
# APP_JAR           (Mendatory)  app jar   ex) /tos/offering_m/tos/TosStreamApp/lib/TosStreamApp.jar
# JARS              (Optional)   Comma-separated list of jars to include on the driver
#                                and executor classpaths
# CONF              (Optional)   Arbitrary Spark configuration property.
# FILES             (Optional)   Comma-separated list of files to be placed in the working
#                                directory of each executor. File paths of these files
#                                in executors can be accessed via SparkFiles.get(fileName).
# MASTER            (Optional)   default 'yarn'
# DEPLOY_MODE       (Optional)   default 'client'
# DRIVER_MEMORY     (Optional)   default '2G'
# NUM_EXECUTOR      (Optional)   default '3'
# EXECUTOR_CORE     (Optional)   default '4'
# EXECUTOR_MEMORY   (Optional)   default '4G'
# QUEUE             (Optional)   default 'default'
#------------------------------------------------------------------------------

Check_StreamApp() {
  _APP_CNT=`yarn --loglevel WARN application -list -appTypes SPARK | grep $1 | grep -v grep | wc -l` 
  echo $_APP_CNT 
}

Kill_StreamApp() {
  _APP_ID=`yarn --loglevel WARN application -list -appTypes SPARK | grep $1 | grep -v grep | awk '{print $1}'`
  yarn application -kill $_APP_ID >> ${APP_LOG} 2>&1
}

APP_LOG="$APP_HOME"/logs/"$APP_NAME"_$(date +"%Y%m%d").log

# Only set HDP_HOME if not already set
if [ -z "$HDP_HOME" ] ; then
  HDP_HOME="/usr/hdp/2.6.5.0-292"
fi

# Only set SPARK_HOME if not already set
if [ -z "$SPARK_HOME" ] ; then
  SPARK_HOME="$HDP_HOME/spark2"
fi

# Add on extra jar files to JARS
if [ ! -z "$JARS" ] ; then
  JARS="$JARS",
fi

JARS="$JARS""$TOS_HOME"/lib/config-1.3.3.jar
JARS="$JARS","$TOS_HOME"/lib/ficus_2.11-1.1.1.jar
JARS="$JARS","$TOS_HOME"/lib/kafka-clients-0.10.0.1.jar
JARS="$JARS","$TOS_HOME"/lib/ojdbc8-12.2.0.1.jar
JARS="$JARS","$TOS_HOME"/lib/play-functional_2.11-2.6.9.jar
JARS="$JARS","$TOS_HOME"/lib/play-json_2.11-2.6.9.jar
JARS="$JARS","$TOS_HOME"/lib/spark-streaming-kafka-0-10_2.11-2.3.0.jar

if [ -z "$MASTER" ] ; then
  MASTER="yarn"
fi

if [ -z "$DEPLOY_MODE" ] ; then
  DEPLOY_MODE="client"
fi

if [ -z "$DRIVER_MEMORY" ] ; then
  DRIVER_MEMORY="2G"
fi

if [ -z "$NUM_EXECUTOR" ] ; then
  NUM_EXECUTOR="3"
fi

if [ -z "$EXECUTOR_CORE" ] ; then
  EXECUTOR_CORE="4"
fi

if [ -z "$EXECUTOR_MEMORY" ] ; then
  EXECUTOR_MEMORY="4G"
fi

if [ -z "$QUEUE" ] ; then
  QUEUE="default"
fi

if [ -z "$FILES" ] ; then
  FILES="--files $FILES"
fi

PARAMS="--name ${APP_NAME}"
PARAMS="$PARAMS --master $MASTER"
PARAMS="$PARAMS --queue $QUEUE"
PARAMS="$PARAMS --master $MASTER"
PARAMS="$PARAMS --deploy-mode $DEPLOY_MODE"
PARAMS="$PARAMS --driver-memory $DRIVER_MEMORY"
PARAMS="$PARAMS --num-executors $NUM_EXECUTOR"
PARAMS="$PARAMS --executor-cores $EXECUTOR_CORE"
PARAMS="$PARAMS --executor-memory $EXECUTOR_MEMORY"

if [ ! -z "$CONF" ] ; then
  PARAMS="$PARAMS --conf $CONF"
fi

if [ ! -z "$FILES" ] ; then
  PARAMS="$PARAMS --files $FILES"
fi

PARAMS="$PARAMS --jars $JARS"
PARAMS="$PARAMS --class $APP_CLASS"
PARAMS="$PARAMS $APP_JAR"

echo "#### $APP_NAME Configurations ####" 
echo "HDP_HOME        : $HDP_HOME"
echo "SPARK_HOME      : $SPARK_HOME"
echo "TOS_HOME        : $TOS_HOME"
echo "APP_HOME        : $APP_HOME"
echo "APP_CLASS       : $APP_CLASS"
echo "APP_NAME        : $APP_NAME"
echo "APP_JAR         : $APP_JAR"
echo "APP_LOG         : $APP_LOG"
echo "JARS            : $JARS"
echo "CONF            : $CONF"
echo "FILES           : $FILES"
echo "MASTER          : $MASTER"
echo "DEPLOY_MODE     : $DEPLOY_MODE"
echo "DRIVER_MEMORY   : $DRIVER_MEMORY"
echo "NUM_EXECUTOR    : $NUM_EXECUTOR"
echo "EXECUTOR_CORE   : $EXECUTOR_CORE"
echo "EXECUTOR_MEMORY : $EXECUTOR_MEMORY"
echo "QUEUE           : $QUEUE"
echo "#######\n\n"

if [ "$1" = "run" ] ; then

  if [ `Check_StreamApp $APP_NAME` -gt 0 ] ; then 
    echo "* $APP_NAME appears to still be running. Start aborted."
    exit 1
  else
    $SPARK_HOME/bin/spark-submit $PARAMS
  fi

elif [ "$1" = "start" ] ; then

  if [ `Check_StreamApp $APP_NAME` -gt 0 ] ; then
    echo "* $APP_NAME appears to still be running. Start aborted."
    exit 1
  else
    echo "- $APP_NAME application starting..." 
    nohup $SPARK_HOME/bin/spark-submit $PARAMS >> $APP_LOG 2>&1 & 
  fi

elif [ "$1" = "stop" ] ; then

   if [ `Check_StreamApp $APP_NAME` -eq 0 ] ; then
     echo "* Is $APP_NAME running? Stop aborted."
     exit 1
   fi

  _APP_ID=`yarn --loglevel WARN application -list -appTypes SPARK | grep $APP_NAME | grep -v grep | awk '{print $1}'`
  yarn application -kill $_APP_ID >> $APP_LOG 2>&1 &
  # kill -9 pid

  KILL_SLEEP_INTERVAL=5
  while [ $KILL_SLEEP_INTERVAL -ge 0 ] ; do
    if [ `Check_StreamApp $APP_NAME` -eq 0 ] ; then
      echo "- The $APP_NAME process has been killed."
      break
    fi
  
    if [ $KILL_SLEEP_INTERVAL -ge 0 ] ; then
      sleep 1
    fi

    KILL_SLEEP_INTERVAL=`expr $KILL_SLEEP_INTERVAL - 1`
    if [ $KILL_SLEEP_INTERVAL -lt 0 ] ; then
      echo "* $APP_NAME has not been kill completely yet."
    fi
	
  done

else
  echo "Usage: tos-stream-app.sh(commands ...)"
  echo "commands:"
  echo "run Start tos-stream-app in the current window"
  echo "start Start tos-stream-app in a seperate window"
  echo "stop Stop tos-stream-app, waiting up to 5seconds for the process to end"
  exit 1  
fi

