#!/bin/sh

CUR=`dirname ${0}`

if [ $# -eq 1 ]; then
  OTS_PORT=$1
  echo OTS_PORT: $1
else
  echo "Please input OTS PORT"
  exit 1
fi

RAPIDCOLLECTOR_VERSION=`cat "${CUR}/version.txt"`

# Start Proxy for OTS
docker container ls -a --format="{{.Names}}" | grep Proxy
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "Stop Existing Container Proxy(httpd)."
    docker container stop Proxy && docker container rm Proxy
fi
echo "Run Container Proxy(httpd)."
docker run -d \
--name Proxy \
--network rss \
--restart always \
--mount type=bind,source=/CANON,target=/CANON \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
--add-host=hostsystem:`docker network inspect rss | grep "Gateway" | awk '{print $2}' | sed 's/\"//g'` \
-p ${OTS_PORT}:80 \
rssproxy:${RAPIDCOLLECTOR_VERSION}

RET=$?
if [ ${RET} -ne 0 ]
then
    echo "Starting Proxy(httpd) failed."
fi
