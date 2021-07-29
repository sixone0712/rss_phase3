#!/bin/bash
cd ~postgres/
su - postgres -c 'psql -d rssdb -a -f /tmp/rss_dbupdate.sql'
