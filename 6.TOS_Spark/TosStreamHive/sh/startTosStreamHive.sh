#!/bin/sh

TOS_HOME="/home/offering_m/tos"
APP_NAME="TosStreamHive"
APP_HOME="$TOS_HOME"/"$APP_NAME"
APP_CLASS="com.sktelecom.tos.stream.hive.HiveStreamApp"
APP_JAR="$APP_HOME"/lib/"$APP_NAME"-1.0-SNAPSHOT.jar

. "$TOS_HOME"/bin/tos-stream-app.sh stop
