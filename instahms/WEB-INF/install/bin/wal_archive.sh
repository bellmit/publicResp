#!/bin/bash

#
# Callback hook for postgresql to call us when a WAL file is ready to be backed up.
# Hot backups are stored under the hot_backups directory, under the directory,
# /var/lib/postgresql/8.4. This is the home directory of the user "postgres", which is 
# what we will be running as.
#
# This script is copied to /var/lib/postgresq/8.4 since /root isn't accessible normally
# by other users, including postgres.
#

[ -f /etc/hms/options ] && source /etc/hms/options

# The standby does not need to archive, even if postgresql.conf has been setup to do so.
# A standby's postgresql.conf must have archive mode enabled, because when it becomes
# active, it needs to archive logs to the disk.
[ "$HA_IS_STANDBY" == 'Y' ] && exit 0

if [ -e /usr/lib/postgresql/10.5 ] ; then
	PG_VER=10.5
elif [ -e /usr/lib/postgresql/9.3 ] ; then
	PG_VER=9.3
fi
PG_DIR=/var/lib/postgresql/$PG_VER

rsync=/usr/bin/rsync
rc="ssh $HA_OTHER_NODE"
rotating=/var/run/hot_backup_rotating

# return error if the base backup is being rotated (delete old rotating file, it could be bogus)
find $rotating -mtime +0 -exec  rm '{}' ';' >/dev/null 2>&1
[ -f $rotating ] && echo "WAL Archive: archiving paused by $rotating" >&2 && exit 19

if [ ! -z "$HA_OTHER_NODE" -a "$HA_FAILED_STATE" != 'Y' ] ; then
	# rsync the file to the standby server: we're running as user postgres, so we can only
	# access our own directory. Also, postgres user has to have a pwdless login to standby
	$rsync -a $1 $HA_OTHER_NODE:$PG_DIR/ha_backups/backup/wal/$2

else
	# HB option, or HA in failed state. We archive locally on a secondary disk
	bkpdir=$PG_DIR/hb_backups/backup
	mkdir -p $bkpdir/wal
	cp -a $1 $bkpdir/wal/$2
fi

