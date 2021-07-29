#!/bin/sh

CUR=`dirname ${0}`
LOGMONITOR_VERSION=`cat "${CUR}/version.txt"`


echo "##########################################################"
echo "  UPDATE LOG MONITOR SERVER DOCKER IMAGES"
echo "##########################################################"

echo "##########################################################"
echo "  DELETE EXISTING DOCKER IMAGES"
echo "##########################################################"
#docker stop $(docker ps -aq)
#docker container prune -f
#docker image prune -a -f
docker stop Log-Monitor-Proxy Log-Monitor-Server Log-Monitor-Database
docker rm Log-Monitor-Proxy Log-Monitor-Server Log-Monitor-Database
docker rmi $(docker images --filter=reference="log-monitor*" -q)

### Docker Image Load ###
# Docker Image Load
echo "##########################################################"
echo "  LOAD DOCKER IMAGES FROM ARCHIVE."
echo "##########################################################"
docker image load -i "${CUR}/logmonitor_images_${LOGMONITOR_VERSION}.tar.gz"


echo "##########################################################"
echo "  Start Log Monitor Containers"
echo "##########################################################"
mkdir -p /LOG/CANON

readlink -s /CANON
RET=$?
if [ ${RET} -ne 0 ]
then
    ln -s /LOG/CANON /CANON
fi

# Start Containers
mkdir -p /CANON/LOGMONITOR /CANON/CRAS/APP /CANON/LOGMONITOR/DEVLOG /CANON/LOGMONITOR/FILES/downloads /CANON/LOGMONITOR/FILES/uploads /CANON/LOGMONITOR/DB /CANON/LOGMONITOR/DEVLOG/tomcat /CANON/LOGMONITOR/DEVLOG/httpd

### Copy CRAS Source File ###
cp -f ${CUR}/cras.tar /CANON/CRAS/APP

sh ${CUR}/run_logmonitor.sh ${LOGMONITOR_VERSION}

# Prune unused image.
#docker image prune -a -f
# Prune dangling image.
docker image prune -f

echo "##########################################################"
echo "  UPDATE SUCCESS!!!"
echo "##########################################################"
