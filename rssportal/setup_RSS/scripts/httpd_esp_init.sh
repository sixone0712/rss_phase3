#!/bin/sh

CUR=`dirname ${0}`

echo
echo START: httpd_esp_init.sh
echo

HOSTNAME=`hostname -I`

echo "HOSTNAME : $HOSTNAME"

echo "creating /etc/httpd/conf.d/timeout.conf"
echo "
Timeout 600
" > /etc/httpd/conf.d/timeout.conf

echo "creating /etc/httpd/conf.d/rss-accctrl.conf"
echo "
<Location /rss>
    ProxyPass ajp://localhost:8020/rss
    
    Order deny,allow
    Deny from all
    Allow from 127.0.0.1
    Allow from ${HOSTNAME}
    Allow From ALL

</Location>" > /etc/httpd/conf.d/rss-accctrl.conf

echo "creating /etc/httpd/conf.d/fsm.conf"
echo "
<Location /fsm>
    ProxyPass ajp://localhost:8020/fsm
    
    Order deny,allow
    Deny from all
    Allow from 127.0.0.1
    Allow from ${HOSTNAME}
    Allow From ALL

</Location>" > /etc/httpd/conf.d/fsm.conf

echo "creating /etc/httpd/conf.d/fsc.conf"
echo "
<Location /fsc>
    ProxyPass ajp://localhost:8020/fsc
    
    Order deny,allow
    Deny from all
    Allow from 127.0.0.1
    Allow from ${HOSTNAME}
    Allow From ALL

</Location>" > /etc/httpd/conf.d/fsc.conf

echo
echo END: httpd_esp_init.sh
echo
