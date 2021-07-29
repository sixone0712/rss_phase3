#!/bin/sh

# COPY server.xml

echo
echo START: tomcat9_esp_init.sh
echo

CUR=`dirname ${0}`

\cp -f ${CUR}/../data/esp/server.xml /usr/local/tomcat9/apache-tomcat-9.0.35/conf/server.xml
chown tomcat:tomcat /usr/local/tomcat9/apache-tomcat-9.0.35/conf/server.xml

echo
echo END: tomcat9_esp_init.sh
echo
