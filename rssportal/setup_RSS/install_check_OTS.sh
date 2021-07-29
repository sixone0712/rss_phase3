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
if [ ${RET} -ne 0 ]
then
    echo "This System is not OTS."
    echo "Installation Check is invalid. Cannot check RSS OTS Installation."
    exit 1
fi


RESULT=0

echo
echo "1. Tomcat9 Installation Check"
echo

test -d /usr/local/tomcat9/apache-tomcat-9.0.35
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- Tomcat Installation : OK"
else
    echo "- Tomcat Installation : NG"
    RESULT=-1
fi

test -f /usr/local/tomcat9/apache-tomcat-9.0.35/bin/setenv.sh
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- setenv.sh Installation : OK"
else
    echo "- setenv.sh Installation : NG"
    RESULT=-1
fi

test -f /usr/local/tomcat9/apache-tomcat-9.0.35/webapps/fsc.war
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- fsc.war Installation : OK"
else
    echo "- fsc.war Installation : NG"
    RESULT=-1
fi

test -f /usr/lib/systemd/system/tomcat9.service
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- tomcat9.service Installation : OK"
else
    echo "- tomcat9.service Installation : NG"
    RESULT=-1
fi




echo
echo "2. Apache Httpd Installation Check"
echo

HOSTNAME=`hostname -I`

test -f /etc/httpd/conf.d/fsc.conf
RET=$?
if [ ${RET} -eq 0 ]
then
    TMP=`cat /etc/httpd/conf.d/fsc.conf | grep $HOSTNAME`
    if [ ${RET} -eq 0 ]
    then
        echo "- fsc.conf Installation : OK"
    else
        echo "- fsc.conf Installation : NG"
        RESULT=-1
    fi
else
    echo "- fsc.conf Installation : NG"
    RESULT=-1
fi

test -f /etc/httpd/conf.d/timeout.conf
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- timeout.conf Installation : OK"
else
    echo "- timeout.conf Installation : NG"
    RESULT=-1
fi



echo
echo "3. Startup/Shutdown Script Check"
echo

TMP=`cat /usr/local/canon/ots/canon/ees/tools/Startup/service_ctrl.sh | grep "systemctl start tomcat9"`
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- tomcat9 service start : OK"
else
    echo "- tomcat9 service start : NG"
    RESULT=-1
fi

TMP=`cat /usr/local/canon/ots/canon/ees/tools/Startup/service_ctrl.sh | grep "systemctl stop tomcat9"`
RET=$?
if [ ${RET} -eq 0 ]
then
    echo "- tomcat9 service stop : OK"
else
    echo "- tomcat9 service stop : NG"
    RESULT=-1
fi




if [ ${RESULT} -eq 0 ]
then
    echo
    echo OTS Install Check Success. All Component is installed.
    echo
else
    echo
    echo OTS Install Check Fail. At least One Component is not installed.
    echo
fi
