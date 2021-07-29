
mkdir -p /CANON/ENV /CANON/DEVLOG /CANON/LOG/downloads /CANON/DB /CANON/WORKING /CANON/DEVLOG/tomcat /CANON/LOG/cache

docker network create rss

# Start Database
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

# Start Rapid-Collector
docker run -d \
--name Rapid-Collector \
--network rss \
--restart always \
--tmpfs /tmp \
--tmpfs /run \
--mount type=bind,source=/CANON,target=/CANON \
-e SPRING_PROFILES_ACTIVE=release \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
rapid-collector:latest

# Start ServiceManager for ESP
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
  servicemanager_esp:latest
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
  servicemanager_esp:latest
fi

# Start Proxy for ESP
docker run -d \
--name Proxy \
--network rss \
--restart always \
--mount type=bind,source=/CANON,target=/CANON \
-e TZ=`timedatectl | grep "Time zone" | awk '{printf $3}'` \
-p 80:80 \
rssproxy:latest


