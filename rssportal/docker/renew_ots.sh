CUR=`dirname ${0}`

docker pull 10.1.31.230/rss/fileservicecollect:latest
docker pull 10.1.31.230/rss/servicemanager_ots:latest
docker pull 10.1.31.230/rss/rssproxy:latest

docker tag 10.1.31.230/rss/fileservicecollect:latest fileservicecollect:latest
docker tag 10.1.31.230/rss/servicemanager_ots:latest servicemanager_ots:latest
docker tag 10.1.31.230/rss/rssproxy:latest rssproxy:latest

sh "${CUR}/stop_ots.sh"
sh "${CUR}/run_ots.sh"

docker image prune --all --force
docker volume prune --force
