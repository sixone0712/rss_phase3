#!/bin/sh

echo
echo START: tomcat9_init.sh
echo

CUR=`dirname ${0}`

\rm -rf /usr/local/tomcat9
echo "make directory : /usr/local/tomcat9"
mkdir -p /usr/local/tomcat9

echo "extract apache-tomcat-9.0.35.tar.gz to /usr/local/tomcat9."
tar -xvzf ${CUR}/../apache-tomcat-9.0.35.tar.gz -C /usr/local/tomcat9

echo "creating /usr/local/tomcat9/apache-tomcat-9.0.35/bin/setenv.sh ."
echo "
UMASK=\"0022\"
JAVA_HOME=/usr/lib/jvm/jre
CATALINA_PID=\"$CATALINA_BASE/tomcat.pid\"
JAVA_OPTS=\"\$JAVA_OPTS -Dspring.profiles.active=release\"
" > /usr/local/tomcat9/apache-tomcat-9.0.35/bin/setenv.sh

chown -R tomcat:tomcat /usr/local/tomcat9

test -f /usr/lib/systemd/system/tomcat9.service
if [ $? -ne 0 ]
then
    echo "creating /usr/lib/systemd/system/tomcat9.service."
    echo "
[Unit]
Description=tomcat9
After=network.target syslog.target

[Service]
Type=forking
User=tomcat
Group=tomcat

ExecStart=/usr/local/tomcat9/apache-tomcat-9.0.35/bin/startup.sh
ExecStop=/usr/local/tomcat9/apache-tomcat-9.0.35/bin/shutdown.sh

Umask=0022
RestartSec=10
Restart=always

[Install]
WantedBy=multi-user.target
" > /usr/lib/systemd/system/tomcat9.service
else
    echo "/usr/lib/systemd/system/tomcat9.service is already exists."
fi

systemctl daemon-reload

echo
echo END: tomcat9_init.sh
echo
