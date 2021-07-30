#!/bin/bash

#
# Hot backup: rotate saved hot backups, take a new base backup of the pg cluster.
# This is run on the standby in case of HA, and the only server in case of HB.
# If HB, it is useful only if the backup is on a secondary disk.
#
# This is typically run as a daily/weekly cron-job, but also can be used
# to kick start and save the initial backup during setup. For HA, it is weekly
# and for HB it is normally weekly but daily if S3 backup is enabled.
#
# Please see ha_hb_readme.txt for details.
#

source `dirname $0`/functions

# prefs
[ -z $HOT_BACKUP_SAVE_COUNT ] && HOT_BACKUP_SAVE_COUNT=1
[ -z $WEEK_BEG ] && WEEK_BEG=Mon

[ "$HOT_BACKUP_ENABLED" != "Y" ] && date_echo "Hot backup not enabled" && exit 0
! is_default_app && date_echo "Can run only from default app, exiting" && exit 0

if [ "$1" != "immediate" ] ; then
	# Weeekly for HA/HB, daily for S3 (Note: S3 backup depends on HB)
	DOW=`date +%a`
	[ -z "$S3_BACKUP_LOCATION" -a "$DOW" != "$WEEK_BEG" ] && exit 0
fi
# else: running from command line for initializing hot_backup.

pidfile=/var/run/hot_backup.pid
rotating=/var/run/hot_backup_rotating
rsync="sudo /usr/bin/rsync"
rc="ssh $HA_OTHER_NODE"

# Use ha_backups if we are a standby, hb_backups otherwise
if [ "$HA_IS_STANDBY" == "Y" ] ; then
	bkpbase=$PG_DIR/ha_backups
else
	bkpbase=$PG_DIR/hb_backups
fi

function start_backup() {
	if [ "$HA_IS_STANDBY" == "Y" ] ; then
		$PG_SCRIPT stop
	else 
		psql -c "SELECT pg_start_backup('hot_backup');"
	fi
}

function stop_backup() {

	if [ "$HA_IS_STANDBY" == "Y" ] ; then
		if [ -f $bkpbase/backup.1/last_redo_file ] ; then
			last_redo_file=`cat $bkpbase/backup.1/last_redo_file`

			if [ -f $bkpbase/backup.1/wal/$last_redo_file ] ; then
				# copy the last redo file to current backup 
				sudo cp $bkpbase/backup.1/wal/$last_redo_file $bkpbase/backup/wal/

				# copy all files newer than last redo file to current backup
				# and ensure postgres user can read it
				find $bkpbase/backup.1/wal/ -type f -newer $bkpbase/backup.1/wal/$last_redo_file \
					-exec cp '{}' $bkpbase/backup/wal/ \;
				sudo chown postgres:postgres $bkpbase/backup/wal/*
			fi
		fi

		$PG_SCRIPT start

	else 
		psql -c "SELECT pg_stop_backup();"
	fi
}

if [ -f $pidfile ] ; then
	pid=`cat $pidfile`
	date_echo "Backup in progress (pid $pid), not initiating another"
	exit 1
fi
echo $$ > $pidfile
[ $? -ne 0 ] && date_echo "Couldn't write to $pidfile. Are we root?" >&2 && exit 1

date_echo "Starting hot backup"

# rotate backups: wal archiving is disabled during this.
# Signal the wal archive command that we are rotating. During this period, no
# new wal files should be copied to ensure a consistent backup. If there is 
# a call to archive the wal file, we should return an error.
if [ "$HA_IS_STANDBY" == "Y" ] ; then
	$rc "sudo touch $rotating"
else
	sudo touch $rotating
fi

# delete the oldest saved backup
sudo rm -rf $bkpbase/backup.$HOT_BACKUP_SAVE_COUNT

# rotate the rest, including the current backup
for (( i=$HOT_BACKUP_SAVE_COUNT ; $i>0 ; i-- )) ; do
	j=$((i-1))
	[ $j -eq 0 ] && sourcebkp="backup" || sourcebkp="backup.$j"
	if [ -e $bkpbase/$sourcebkp ] ; then
		sudo mv $bkpbase/$sourcebkp $bkpbase/backup.$i
	fi
done

# create the current backup directory (empty)
sudo mkdir -p $bkpbase/backup/data
sudo mkdir -p $bkpbase/backup/wal
sudo chown -R postgres:postgres $bkpbase/*

# We need to remove the signal because start/stop backup requires wal archiver to function
# for a local hot backup. But not for standby.
if [ "$HA_IS_STANDBY" != "Y" ] ; then
	sudo rm $rotating
fi

#
# There is a small window here where a genuine wal archive callback can happen.
# It will be written to the current backup directory, whereas it belongs to backup.1.
# But this should be harmless, with the only side effect that we cannot recover to
# a point within this period (should be possible by manually moving the file, though)
#

start_backup

# copy the data into current backup's data area
date_echo "Syncing base data (this can take some time)"
$rsync -a --exclude='pg_xlog/*' $PG_DIR/main/ $bkpbase/backup/data/

stop_backup

# For standby, this is the time to remove the rotating signal
if [ "$HA_IS_STANDBY" == "Y" ] ; then
	$rc "sudo rm $rotating"
fi

# all done
sudo rm -f $pidfile
date_echo "Done creating backup"

#
# Save it in S3 if the option is Y. To set up Amazon S3 backup:
#
# 1. In AWS S3, create a directory in a region's bucket named same as hostname.
#    This can be under local or under us (for disaster recovery enabling)
#
# 2. Use IAM to create a group for the customer/schema, eg, MyDentistS3, give permission
#    for the customer directory (copy from other group permissions and change the name).
#
# 3. Create IAM user for the customer/schema, eg, mydentist and generate access key.
#    Save the Access Key Id and Secret Access Key in /etc/hms/options as
#     export AWS_ACCESS_KEY_ID=<key id>
#     export AWS_SECRET_ACCESS_KEY=<secret key>
#
# 4. Install duplicity:
#    apt-get install duplicity
#
# 5. Set the option S3_BACKUP_LOCATION in /etc/hms/options to us or local.
#
if [ ! -z "$S3_BACKUP_LOCATION" ] ; then
	s3dir="s3+http://instabackups-${S3_BACKUP_LOCATION}/${HOST}"
	options="--no-encryption --s3-use-new-style"
	date_echo "S3: Backing up to $s3dir"
	if [ $DOW == $WEEK_BEG ] ; then
		date_echo "S3: Doing full backup"
		action=full
	fi
	/usr/bin/duplicity $action $options $bkpbase/backup $s3dir
	if [ $? -ne 0 ] ; then
		# signal an error to the healthcheck monitor
		echo "S3 backup error" >> $bkperrlog
		tail -20 /var/log/insta/$APP/backup.log >> $bkperrlog
	else
		if [ $DOW == $WEEK_BEG ] ; then
			date_echo "S3: Cleaning up older backups"
			/usr/bin/duplicity $options remove-all-but-n-full 1 --force $s3dir
			/usr/bin/duplicity $options cleanup --force $s3dir
		fi
	fi
	date_echo "S3: done."
fi

