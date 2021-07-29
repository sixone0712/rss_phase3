CUR=`dirname ${0}`


if [ $# -eq 0 ]; then
  test -f "${CUR}/version.txt"
  RET=$?
  if [ ${RET} -eq 0 ];
  then
    RAPIDCOLLECTOR_VERSION=`cat "${CUR}/version.txt"`
  else
    RAPIDCOLLECTOR_VERSION="latest"
  fi
elif [ $# -eq 1 ]; then
  RAPIDCOLLECTOR_VERSION=$1
else
  echo "invalid argument count"
  exit 1
fi

test -f "${CUR}/ots_port.txt"
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ots_port is not exists..."
    echo "Install default port 80."
    OTS_PORT=80
else
  OTS_PORT=`cat "${CUR}/ots_port.txt"`
fi

docker network ls --format "{{.Name}}" | grep rss
RET=$?
if [ ${RET} -ne 0 ]
then
    docker network create rss
    RET=$?
    if [ ${RET} -ne 0 ]
    then
        echo "\"docker network create rss\" failed."
    fi
fi

# Start FileServiceCollect
docker container ls -a --format="{{.Names}}" | grep FileServiceCollect
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "Stop Existing Container FileServiceCollect."
    docker container stop FileServiceCollect && docker container rm FileServiceCollect
fi
echo "Run Container FileServiceCollect."
docker run -d \
--name FileServiceCollect \
--network rss \
--restart always \
--tmpfs /tmp \
--tmpfs /run \
--mount type=bind,source=/CANON,target=/CANON \
-e SPRING_PROFILES_ACTIVE=release \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
--cap-add=SYS_NICE \
fileservicecollect:${RAPIDCOLLECTOR_VERSION}

RET=$?
if [ ${RET} -ne 0 ]
then
    echo "Starting FileServiceCollect failed."
fi


# Start ServiceManager for OTS
docker container ls -a --format="{{.Names}}" | grep ServiceManager
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "Stop Existing Container ServiceManager."
    docker container stop ServiceManager && docker container rm ServiceManager
fi
echo "Run Container ServiceManager."
test -d /usr/local/canon/esp/Logs
RET=$?
if [ ${RET} -eq 0 ]
then
  docker run -d \
  --name ServiceManager \
  --network rss \
  --restart always \
  --tmpfs /tmp \
  --tmpfs /run \
  --mount type=bind,source=/var/run/docker.sock,target=/var/run/docker.sock \
  --mount type=bind,source=/CANON,target=/CANON \
  --mount type=bind,source=/usr/local/canon/esp/Logs,target=/CANON/DEVLOG/legacy \
  -e SPRING_PROFILES_ACTIVE=devots \
  -e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
  --add-host=hostsystem:`docker network inspect rss | grep "Gateway" | awk '{print $2}' | sed 's/\"//g'` \
  servicemanager_ots:${RAPIDCOLLECTOR_VERSION}
else
  docker run -d \
  --name ServiceManager \
  --network rss \
  --restart always \
  --tmpfs /tmp \
  --tmpfs /run \
  --mount type=bind,source=/var/run/docker.sock,target=/var/run/docker.sock \
  --mount type=bind,source=/CANON,target=/CANON \
  -e SPRING_PROFILES_ACTIVE=devots \
  -e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
  --add-host=hostsystem:`docker network inspect rss | grep "Gateway" | awk '{print $2}' | sed 's/\"//g'` \
  servicemanager_ots:${RAPIDCOLLECTOR_VERSION}
fi

RET=$?
if [ ${RET} -ne 0 ]
then
    echo "Starting ServiceManager failed."
fi


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
