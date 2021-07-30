#!/bin/bash

#
# Cleans up tomcat logs periodically. We need to do this because although the
# logs are rotated, old files are kept indefinitely.
#
# Will be run as a cron-job, on all servers at midnight.
#

HOST=`hostname`
HOST=${HOST#instahms-}
source `dirname $0`/functions

#
# Run the following only once for all apps: exit if not the main app
#
is_default_app || exit 0

date_echo "Cleaning up old logs under tomcat: "
find $TOMCAT_HOME/logs/ -mtime +31 -exec rm '{}' ';'

date_echo "Cleaning up old files under tomcat/temp: "
find $TOMCAT_HOME/temp/ -mtime +2 -exec rm '{}' ';'

