#!/bin/bash

pidfile=$1

[ -f log/java0.log.0 ] && mv log/java0.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
/usr/local/openjdk11/bin/java -Xms512m -Xmx2g -jar l2jserver.jar > log/stdout.log &
echo $! > ${pidfile}
