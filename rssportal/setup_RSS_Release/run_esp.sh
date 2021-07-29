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

docker network ls --format "{{.Name}}" | grep rss
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "Create Docker Network rss."
    docker network create rss
    RET=$?
    if [ ${RET} -ne 0 ]
    then
        echo "\"docker network create rss\" failed."
    fi
fi


# Start Database
docker container ls -a --format="{{.Names}}" | grep Database
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "Stop Existing Container Database."
    docker container stop Database && docker container rm Database
fi
echo "Run Container Database."
docker run -d \
--name Database \
--network rss \
--restart always \
--mount type=bind,source=/CANON,target=/CANON \
--mount type=bind,source=/CANON/DB,target=/var/lib/postgresql/data \
-e POSTGRES_USER=rssadmin \
-e POSTGRES_PASSWORD=1234 \
-e POSTGRES_DB=rssdb \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
--cap-add=SYS_PTRACE \
-p 5432:5432 \
postgres:13-alpine

sleep 10s

# Start Rapid-Collector
docker container ls -a --format="{{.Names}}" | grep Rapid-Collector
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "Stop Existing Container Rapid-Collector."
    docker container stop Rapid-Collector && docker container rm Rapid-Collector
fi
echo "Run Container Rapid-Collector."
docker run -d \
--name Rapid-Collector \
--network rss \
--restart always \
--tmpfs /tmp \
--tmpfs /run \
--mount type=bind,source=/CANON,target=/CANON \
-e SPRING_PROFILES_ACTIVE=release \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
rapid-collector:${RAPIDCOLLECTOR_VERSION}

# Start ServiceManager for ESP
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
  --mount type=bind,source=/CANON,target=/CANON \
  --mount type=bind,source=/usr/local/canon/esp/Logs,target=/CANON/DEVLOG/legacy \
  --mount type=bind,source=/var/run/docker.sock,target=/var/run/docker.sock \
  -e SPRING_PROFILES_ACTIVE=devesp \
  -e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
  --add-host=hostsystem:`docker network inspect rss | grep "Gateway" | awk '{print $2}' | sed 's/\"//g'` \
  servicemanager_esp:${RAPIDCOLLECTOR_VERSION}
else
  docker run -d \
  --name ServiceManager \
  --network rss \
  --restart always \
  --tmpfs /tmp \
  --tmpfs /run \
  --mount type=bind,source=/CANON,target=/CANON \
  --mount type=bind,source=/var/run/docker.sock,target=/var/run/docker.sock \
  -e SPRING_PROFILES_ACTIVE=devesp \
  -e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
  --add-host=hostsystem:`docker network inspect rss | grep "Gateway" | awk '{print $2}' | sed 's/\"//g'` \
  servicemanager_esp:${RAPIDCOLLECTOR_VERSION}
fi


# Start Proxy for ESP
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
-p 80:80 \
rssproxy:${RAPIDCOLLECTOR_VERSION}


