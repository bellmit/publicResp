#!/bin/bash

#
# SR Init: initialize a secondary server for streaming replication
#  * create required directories on the secondary
#  * sync the initial data to the secondary from the primary
#  * start the secondary in standby mode, so that it catches up with the primary
#

source `dirname $0`/functions

rsync=/usr/bin/rsync
bkpbase=$PG_DIR/ha_backups

# Streaming replication setup has to be done on secondary and not on primary.
# So we do not need a remote command.
rc=""

[ "$HA_IS_STANDBY" != 'Y' ] && echo "Streaming replication initialization is not allowed on a non-standby, exiting" && exit 1

# Ensure postgres is stopped, since we're going to transfer data to its data dir
echo "Stopping postgres on secondary"
$PG_SCRIPT stop

# Ensure backup directory exists. wal archiver will fail if it doesn't
sudo mkdir -p $bkpbase/backup/data
sudo mkdir -p $bkpbase/backup/wal
sudo chown -R postgres:postgres $bkpbase

# start clean
sudo rm -f $bkpbase/backup/wal/*

# start the backup process

#run the pg_basebackup from the standby on the primary
sudo rm -rf $PG_DIR/main/*
sudo pg_basebackup -h $HA_OTHER_NODE -D $PG_DIR/main/ -x
if [ $? -ne 0 ] ; then
	# error other than partial transfer due to vanished source files
	echo "ERROR in transferring files, cannot setup SR"
	exit 1
fi

#pg_basebackup does not set the file permissions, we set it explicitly
sudo chown -R postgres:postgres $PG_DIR/main/

# we removed everything from the cluster, set back the crt and key links. without these PG will not start
sudo ln -s /etc/ssl/certs/ssl-cert-snakeoil.pem $PG_DIR/main/server.crt
sudo ln -s /etc/ssl/private/ssl-cert-snakeoil.key $PG_DIR/main/server.key

sudo cp $APPHOME/instahms/WEB-INF/install/recovery.conf.sr $PG_DIR/main/recovery.conf
sudo sed --in-place -e s/__HA_OTHER_NODE__/$HA_OTHER_NODE/g $PG_DIR/main/recovery.conf
sudo chown postgres:postgres $PG_DIR/main/recovery.conf

# On secondary, /etc/hms/options has to be set for standby mode
sudo sed --in-place -e '/HA_IS_STANDBY/d' /etc/hms/options
echo HA_IS_STANDBY=Y >> /etc/hms/options

echo "Restarting postgres on secondary .."
sudo rm -f /tmp/pgsql.failover
$PG_SCRIPT start

[ $? -ne 0 ] && echo "ERROR starting postgresql on remote, setup failed" && exit 1

