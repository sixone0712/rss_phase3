CUR=`dirname ${0}`

docker pull 10.1.31.230/rss/postgres:13-alpine
docker pull 10.1.31.230/rss/rapid-collector:latest
docker pull 10.1.31.230/rss/servicemanager_esp:latest
docker pull 10.1.31.230/rss/rssproxy:latest

docker tag 10.1.31.230/rss/postgres:13-alpine postgres:13-alpine
docker tag 10.1.31.230/rss/rssproxy:latest rssproxy:latest
docker tag 10.1.31.230/rss/servicemanager_esp:latest servicemanager_esp:latest
docker tag 10.1.31.230/rss/rapid-collector:latest rapid-collector:latest

sh "${CUR}/stop_esp.sh"
sh "${CUR}/run_esp.sh"

docker image prune --all --force
docker volume prune --force
