#!/bin/bash

#
# HA Init: initialize a HA pair from the primary
#  * create required directories on the secondary
#  * sync the initial data to the secondary
# See hot_backup for detailed instructions.
#

source `dirname $0`/functions

if [ -z "$APPHOME" ] ; then
	APPHOME="/root/webapps"
fi


rsync="sudo /usr/bin/rsync"
bkpbase=$PG_DIR/ha_backups


# HA setup has to be done on primary and not on secondary
# So this check is needed.

if [ "$HA_IS_STANDBY" = 'Y' ] ; then
      echo " HA setup is not allowed to do on a Stand By Node"
      exit 0
fi

function start_backup() {
	psql -c "SELECT pg_start_backup('hot_backup');"
}

function stop_backup() {
	psql -c "SELECT pg_stop_backup();"
}

# This is the primary: initial sync to secondary. This must be done within a start/stop
# backup because we are copying live data.
rc="ssh $HA_OTHER_NODE"

# Ensure postgres is stopped, since we're going to transfer data to its data dir
echo "Stopping postgres on secondary"
$rc "$PG_SCRIPT stop"
echo "Stopping tomcat-9 on secondary"
$rc "sudo /etc/init.d/tomcat-9 stop > /dev/null 2>&1"		# ignore errors.

# Ensure backup directory exists. wal archiver will fail if it doesn't
$rc "sudo mkdir -p $bkpbase/backup/data"
$rc "sudo mkdir -p $bkpbase/backup/wal"
$rc "sudo chown -R postgres:postgres $bkpbase"
# start clean
$rc "sudo rm -f $bkpbase/backup/wal/*"

# start the backup process
start_backup

echo "Syncing data to secondary .. (this can take some time, file has vanished errors can be ignored)"
$rsync -az --exclude='pg_xlog/*' --delete --delete-excluded $PG_DIR/main $HA_OTHER_NODE:$PG_DIR/
echo "Saving data as initial backup .. (this can take some time)"

# save the initial copy in the backup area
$rc "$rsync -a --delete $PG_DIR/main/* $bkpbase/backup/data/"
if [ $? -ne 0 -a $? -ne 23 ] ; then
	# error other than partial transfer due to vanished source files
	echo "ERROR in transferring files, cannot setup HA"
	exit 1
fi

# end the backup process
stop_backup
# wal archive should by now have copied the required wal files also to wal directory on secondary.

# On secondary, recovery mode has to be set and postgresql started.
$rc "sudo cp $APPHOME/instahms/WEB-INF/install/recovery.conf.ha $PG_DIR/main/recovery.conf"
$rc "sudo sed --in-place -e s/__pg_ver__/$PG_VER/g $PG_DIR/main/recovery.conf"
$rc "sudo chown postgres:postgres $PG_DIR/main/recovery.conf"

# On secondary, /etc/hms/options has to be set for standby mode
$rc "sudo sed --in-place -e '/HA_IS_STANDBY/d' /etc/hms/options"
$rc "sudo echo HA_IS_STANDBY=Y >> /etc/hms/options"

echo "Restarting postgres on secondary .."
$rc "sudo rm -f /tmp/pgsql.failover"
$rc "$PG_SCRIPT start"

[ $? -ne 0 ] && echo "ERROR starting postgresql on remote, setup failed"
