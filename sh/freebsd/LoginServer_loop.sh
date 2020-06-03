#!/bin/sh

pidfile=$1
user=$2

[ -f log/stdout.log ] && /usr/bin/gzip log/stdout.log && mv log/stdout.log.gz "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log.gz" 
/usr/sbin/daemon -u ${user} /usr/local/openjdk11/bin/java -Xms128m -Xmx256m -jar l2jlogin.jar > log/stdout.log &
let pid=$!+1


echo ${pid} > ${pidfile}

