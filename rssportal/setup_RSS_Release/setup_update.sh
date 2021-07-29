#!/bin/sh

CUR=`dirname ${0}`
RAPIDCOLLECTOR_VERSION=`cat "${CUR}/version.txt"`

if [ $# -eq 1 ]; then
  TARGET=$1
  if [ "${TARGET}" != "ESP" ] && [ "${TARGET}" != "OTS" ]; then
    echo "invalid argument target ${TARGET}"
    exit 1
  fi
else
  echo "invalid argument target"
  exit 1
fi

echo "##########################################################"
echo "  UPDATE ${TARGET} DOCKER IMAGES"
echo "##########################################################"

echo "##########################################################"
echo "  DELETE EXISTING DOCKER IMAGES"
echo "##########################################################"
docker stop $(docker ps -aq)
docker container prune -f
docker image prune -a -f

### Docker Image Load ###
# Docker Image Load
echo "##########################################################"
echo "  LOAD DOCKER IMAGES FROM ARCHIVE."
echo "##########################################################"
docker image load -i "${CUR}/rss_images_${RAPIDCOLLECTOR_VERSION}.tar.gz"


echo "##########################################################"
echo "  Start ${TARGET} Containers"
echo "##########################################################"
mkdir -p /LOG/CANON

readlink -s /CANON
RET=$?
if [ ${RET} -ne 0 ]
then
    ln -s /LOG/CANON /CANON
fi

# Start Containers
if [ "${TARGET}" = "ESP" ]; then
  mkdir -p /CANON/ENV /CANON/DEVLOG /CANON/LOG/downloads /CANON/DB /CANON/WORKING /CANON/DEVLOG/tomcat /CANON/LOG/cache /CANON/search /CANON/machines
  sh ${CUR}/run_esp.sh ${RAPIDCOLLECTOR_VERSION}
elif [ "${TARGET}" = "OTS" ]; then
  mkdir -p /CANON/ENV /CANON/DEVLOG /CANON/LOG/downloads /CANON/WORKING /CANON/DEVLOG/tomcat /CANON/search /CANON/machines
  sh ${CUR}/run_ots.sh ${RAPIDCOLLECTOR_VERSION}
else
  echo "invalid argument target ${TARGET}"
  exit 1
fi

# Prune unused image.
docker image prune -a -f

echo "##########################################################"
echo "  INSTALL SUCCESS!!!"
echo "##########################################################"

echo
echo "END: setup_ESP.sh"
echo
