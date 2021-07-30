#!/bin/bash

#
# Checks whether a restart has been signalled, if so, restart tomcat and remove
# the signal.
#
# This is run as a cron-job so that we can schedule a restart somewhere
# duing the night when there is little or no usage. Typically used when upgrading
# without a restart manually, but since a restart is a good thing, we schedule it for
# later.
#
# Since this is a complete restart, it is run only by the main application: hms
#

source `dirname $0`/functions

#
# disable for non-default apps
#
is_default_app || exit 0

dorestart="N"

#
# Restart if there is a signal: non-regular restart
#
RES_APP_FILE=$APPHOME/restart_app

if [ -f $RES_APP_FILE ] ; then
	date_echo "Restarting app due to signal"
	dorestart="Y"
	sudo rm $RES_APP_FILE
fi

#
# force a restart if we are called that way (every week or two, we can
# force a restart just in case.
#
if [ "$1" == "force" ] ; then 
	date_echo "Restarting app (forced)"
	dorestart="Y"
fi

if [ $dorestart == "Y" ] ; then
	#
	# disable if upgrade is in progress
	#
	UPGRADING_FILE=$APPHOME/upgrading
	if [ -f $UPGRADING_FILE ] ; then
		date_echo "Auto-restart: upgrade in progress, skpping requested restart"
		exit 0
	fi

	date_echo "Stopping tomcat-9"
	sudo /etc/init.d/tomcat-9 stop

	date_echo "Restarting postgres"
	$PG_SCRIPT restart

	if [ "$HA_IS_STANDBY" != 'Y' ] ; then
		date_echo "Starting tomcat-9"
		sudo /etc/init.d/tomcat-9 start
	fi
fi

