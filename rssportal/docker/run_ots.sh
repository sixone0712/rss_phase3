mkdir -p /CANON/ENV /CANON/DEVLOG /CANON/LOG/downloads /CANON/DB /CANON/WORKING /CANON/DEVLOG/tomcat

docker network create rss
# Start FileServiceCollect
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
fileservicecollect:latest

# Start ServiceManager for OTS
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
  servicemanager_ots:latest
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
  servicemanager_ots:latest
fi

# Start Proxy for OTS
docker run -d \
--name Proxy \
--network rss \
--restart always \
--mount type=bind,source=/CANON,target=/CANON \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
-p 80:80 \
rssproxy:latest