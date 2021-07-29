#!/bin/sh

CUR=`dirname ${0}`

echo
echo "START: setup_OTS.sh"
echo

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
if [ ${RET} -ne 0 ]
then
    echo "This System is not OTS."
    echo "Cannot install RSS OTS package."
    exit 1
fi

test -f "${CUR}/version.txt"
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "version.txt is not exists..."
    echo "Installation Failed."
    exit 1
fi
RAPIDCOLLECTOR_VERSION=`cat "${CUR}/version.txt"`
echo "##########################################################"
echo "## Install Rapid-Collector(OTS) $RAPIDCOLLECTOR_VERSION ##"
echo "##########################################################"

echo "##########################################################"
echo "  CHECK EXISTING SYSTEM"
echo "##########################################################"
docker --version
RET=$?
if [ ${RET} -ne 0 ]; then
    echo "Docker System is not installed. Proceed with initial system installation."
else
    echo "Docker System installed. Proceed with only docker image update."
    sh ${CUR}/setup_update.sh OTS
    exit 1
fi

echo "##########################################################"
echo "  Unmount Virtual Images."
echo "##########################################################"
virsh list --all
RET=$?
if [ ${RET} -eq 0 ]
then
  echo "KVM is exited."
  echo "all virtual images is destroy and undefined."
  for i in $(virsh list --all|awk 'NR != 1 {print $2}'|grep -v Name)
  do
    echo ${i}
    virsh destroy ${i}
    virsh undefined ${i}
  done
else
  echo "KVM is not exited."
fi

echo "##########################################################"
echo "  Disable 2020 Phase3 Tomcat9"
echo "##########################################################"
test -d /usr/local/tomcat9/apache-tomcat-9.0.35
RET=$?
if [ ${RET} -ne 0 ]
then
    echo "This System is not 2020 Phase3."
else
    echo "This System is 2020 Phase3."

    echo "Disable 2020 Phase3 Tomcat9"
    systemctl stop tomcat9
    systemctl disable tomcat9
    rm -rf /usr/local/tomcat9/
    rm -rf /etc/logrotate.d/tomcat9

    echo "Delete 2020 Phase3 Http Configuration"
    rm -rf /etc/httpd/conf.d/timeout.conf
    rm -rf /etc/httpd/conf.d/fsc.conf

    echo "Delete 2020 Phase3 service_ctrl.sh"
    rm -rf /usr/local/canon/esp/tools/Startup/service_ctrl.sh
fi

echo "##########################################################"
echo "  CONFIGURE LEGACY HTTPD."
echo "##########################################################"
sh "${CUR}/scripts/httpd_ots_init.sh"
systemctl restart iptables
systemctl restart httpd

systemctl enable postgresql
systemctl enable ees-ftp
systemctl enable fcs
systemctl enable ees-fw
systemctl enable tomcat
systemctl enable httpd

echo "##########################################################"
echo "  INSTALL OR RESTART DOCKER"
echo "##########################################################"
docker --version
RET=$?
if [ ${RET} -ne 0 ]; then
    echo "Docker is not installed. Install Docker first."
    sh "${CUR}/install_docker.sh"
else
  systemctl restart docker
fi

echo "##########################################################"
echo "  MAKE DIRECTORIES."
echo "##########################################################"
mkdir -p /LOG/CANON

readlink -s /CANON
RET=$?
if [ ${RET} -ne 0 ]
then
    ln -s /LOG/CANON /CANON
fi

mkdir -p /CANON/ENV /CANON/DEVLOG /CANON/LOG/downloads /CANON/WORKING /CANON/DEVLOG/tomcat /CANON/search /CANON/machines

echo "##########################################################"
echo "  LOAD DOCKER IMAGES FROM ARCHIVE."
echo "##########################################################"
docker image load -i "${CUR}/rss_images_${RAPIDCOLLECTOR_VERSION}.tar.gz"

echo "##########################################################"
echo "  Start Containers"
echo "##########################################################"
# Start Containers
sh ${CUR}/run_ots.sh ${RAPIDCOLLECTOR_VERSION}
# Prune unused image.
docker image prune -a -f

echo "##########################################################"
echo "  INSTALL SUCCESS!!!"
echo "##########################################################"

echo
echo "END: setup_OTS.sh"
echo