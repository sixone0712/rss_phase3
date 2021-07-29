#!/bin/sh

CUR=`dirname ${0}`

if [ $# -eq 1 ]; then
  LOGMONITOR_PORT=$1
  echo LOGMONITOR_PORT: $1
else
  echo "Please input Log Monitor Server Port"
  exit 1
fi

LOG_MONITOR_VERSION=`cat "${CUR}/version.txt"`

# Start Log-Monitor-Proxy
docker container ls -a --format="{{.Names}}" | grep Log-Monitor-Proxy
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "Stop Existing Container Log-Monitor-Proxy(httpd)."
    docker container stop Log-Monitor-Proxy && docker container rm Log-Monitor-Proxy
fi
echo "Run Container Log-Monitor-Proxy(httpd)."
docker run -d \
--name Log-Monitor-Proxy \
--network rss \
--restart always \
--mount type=bind,source=/CANON,target=/CANON \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
--add-host=hostsystem:`docker network inspect rss | grep "Gateway" | awk '{print $2}' | sed 's/\"//g'` \
-p ${LOGMONITOR_PORT}:80 \
log-monitor-proxy:${LOGMONITOR_VERSION}

RET=$?
if [ ${RET} -ne 0 ]
then
    echo "Starting Log-Monitor-Proxy(httpd) failed."
fi
