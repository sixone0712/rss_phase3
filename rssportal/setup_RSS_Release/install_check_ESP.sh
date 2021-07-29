#!/bin/sh

user=`whoami`
if [ ${user} != "root" ]
then
    echo
    echo "Error: Execution user must be root!"
    echo
    exit 1
fi

test -d /usr/local/canon/ots
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "This System is not ESP."
    echo "Installation Check is invalid. Cannot check RSS ESP Installation."
    exit 1
fi

test -d /usr/local/canon/esp
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "This System is not ESP."
    echo "Installation Check is invalid. Cannot check RSS ESP Installation."
    exit 1
fi

CUR=`dirname ${0}`
RAPIDCOLLECTOR_VERSION=`cat "${CUR}/version.txt"`

echo "##########################################################"
echo "  1. Docker Installation Check"
echo "##########################################################"
docker --version
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : Docker is not installed."
    exit 1
fi

echo "##########################################################"
echo "  2. Docker Image Version Check"
echo "##########################################################"

if [ -n "$RAPIDCOLLECTOR_VERSION" ]; then
  echo RAPIDCOLLECTOR_VERSION: ${RAPIDCOLLECTOR_VERSION}
else
  echo "ERROR : Version in not exist!!"
  exit 1
fi

# Proxy
RET=$(docker container ls --filter "name=Proxy" --format "{{.Image}}")
echo Image: ${RET}

if [ -n "$RET" ]; then
  if [[ "$RET" =~ "${RAPIDCOLLECTOR_VERSION}" ]]; then
    echo "Proxy:${RAPIDCOLLECTOR_VERSION} container is installed"
  else
    echo "ERROR : The version of Proxy container is not" ${RAPIDCOLLECTOR_VERSION}
    exit 1
  fi
else
  echo "ERROR : There is no Proxy container!!"
  exit 1
fi

# Rapid-Collector
RET=$(docker container ls --filter "name=Rapid-Collector" --format "{{.Image}}")
echo Image: ${RET}

if [ -n "$RET" ]; then
  if [[ "$RET" =~ "${RAPIDCOLLECTOR_VERSION}" ]]; then
    echo "Rapid-Collector:${RAPIDCOLLECTOR_VERSION} is installed"
  else
    echo "ERROR : The version of Rapid-Collector is not" ${RAPIDCOLLECTOR_VERSION}
    exit 1
  fi
else
  echo "ERROR : There is no Rapid-Collector Container!!"
  exit 1
fi

# ServiceManager
RET=$(docker container ls --filter "name=ServiceManager" --format "{{.Image}}")
echo Image: ${RET}
if [ -n "$RET" ]; then
  if [[ "$RET" =~ "${RAPIDCOLLECTOR_VERSION}" ]]; then
    echo "ServiceManager:${RAPIDCOLLECTOR_VERSION} is installed"
  else
    echo "ERROR : The version of ServiceManager is not" ${RAPIDCOLLECTOR_VERSION}
    exit 1
  fi
else
  echo "ERROR : There is no ServiceManager Container!!"
  exit 1
fi

# Database
RET=$(docker container ls --filter "name=Database" --format "{{.Image}}")
echo Image: ${RET}
if [ -n "$RET" ]; then
  if [[ "$RET" =~ "13-alpine" ]]; then
    echo "Database:13-alpine is installed"
  else
    echo "ERROR : The version of Database is not 13-alpine"
    exit 1
  fi
else
  echo "ERROR : There is no Database Container"
  exit 1
fi

echo "##########################################################"
echo "  3. Directory Existence Check"
echo "##########################################################"

test -d /CANON/ENV
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/ENV is not installed."
    exit 1
fi

test -d /CANON/DEVLOG
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/DEVLOG is not installed."
    exit 1
fi

test -d /CANON/LOG/downloads
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/LOG/downloads is not installed."
    exit 1
fi

test -d /CANON/DB
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/LOG/DB is not installed."
    exit 1
fi

test -d /CANON/WORKING
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/WORKING is not installed."
    exit 1
fi

test -d /CANON/DEVLOG/tomcat
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/DEVLOG/tomcat is not installed."
    exit 1
fi

test -d /CANON/LOG/cache
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/LOG/cache is not installed."
    exit 1
fi



echo "##########################################################"
echo "  4. Configuration File Existence Check"
echo "##########################################################"
test -f /CANON/ENV/structure.json
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/ENV/structure.json is not installed."
    exit 1
fi

test -f /CANON/ENV/categories.json
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/ENV/categories.json is not installed."
    exit 1
fi

test -f /CANON/ENV/admin_password.json
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : /CANON/ENV/admin_password.json is not installed."
    exit 1
fi

echo "##########################################################"
echo "  5. Http(Legacy) Configuration Check"
echo "##########################################################"

TEMP=`cat /etc/httpd/conf/httpd.conf | grep "Listen 81"`
RET=$?
if [ ${RET} -ne 0 ]; then
  echo "ERROR : Legacy Http Configuration file(/etc/httpd/conf/httpd.conf) is not replaced."
  exit 1
fi

TEMP=`cat /etc/httpd/conf.d/esp-accctrl.conf | grep "Allow from"`
RET=$?
if [ ${RET} -eq 0 ]; then
  echo "ERROR : Legacy Http Configuration file(/etc/httpd/conf.d/esp-accctrl.conf) is not replaced."
  exit 1
fi

TEMP=`cat /etc/httpd/conf.d/fs-accctrl.conf | grep "Allow from"`
RET=$?
if [ ${RET} -eq 0 ]; then
  echo "ERROR : Legacy Http Configuration file(/etc/httpd/conf.d/fs-accctrl.conf) is not replaced."
  exit 1
fi

TEMP=`cat /etc/sysconfig/iptables | grep "dport 81 -j ACCEPT"`
RET=$?
if [ ${RET} -ne 0 ]; then
  echo "ERROR : Old Iptable Configuration file(/etc/sysconfig/iptables) is not replaced."
  exit 1
fi

echo "##########################################################"
echo "  6. Running Container Check"
echo "##########################################################"
docker container ls --format="{{.Names}}" | grep Database
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : Container Database is not running."
    exit 1
fi

docker container ls --format="{{.Names}}" | grep Rapid-Collector
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : Container Rapid-Collector is not running."
    exit 1
fi

docker container ls --format="{{.Names}}" | grep ServiceManager
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : Container ServiceManager is not running."
    exit 1
fi

docker container ls --format="{{.Names}}" | grep Proxy
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "ERROR : Container Proxy(httpd) is not running."
    exit 1
fi

echo "##########################################################"
echo "  Installation Status : OK"
echo "##########################################################"
