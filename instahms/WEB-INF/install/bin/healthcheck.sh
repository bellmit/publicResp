#!/bin/bash

#
# Monitor the health of the system and send alert email if something is
# not looking OK. This should be called daily at 11:00 pm via a cron-job.
#
# The following are checked:
#  1. High CPU usage based on logmon.pl
#  2. Disk space usage in some of the areas that we are interested in
#  3. Errors in HA/HB process 
#  4. tomcat (disappearing or oom) errors
#  (TODO: disk I/O errors?)
#

source `dirname $0`/functions
[ -z "$IT_SUPPORT_MAIL_ID" ] && IT_SUPPORT_MAIL_ID='insta-support@practo.com insta-devops@practo.com'
REPLY_TO="root@hub.instahealthsolutions.com"
CONTENT="/tmp/montext.out"
MESSAGE="/tmp/healthcheck.msg"
echo > $CONTENT

# Disable if not the default app: health check is applicable for the entire server, not each app
is_default_app || exit 0

date_echo "Running health checks"

#
# Check for High CPU usage. More than 10 instances of Action requested from
# logmon.pl qualifies for High CPU Usage.
#
hc=`grep -ci 'Action requested' /var/log/insta/logmon.log`
if [ $hc -gt 10 ] ; then
	subject="High CPU; "
	echo "Frequent High CPU detected:" >> $CONTENT
	grep 'Action requested' /var/log/insta/logmon.log | tail -200 >> $CONTENT
	echo >> $CONTENT
else
	echo "Count of action requested is $hc, not alerting."
fi

#
# Check for disk space > 80% in interested areas: root, db area, db backup area, hb backup area
#
df_files="/ $HMS_WORK_HOME/db-backups /var/lib/postgresql/${PG_VER}/hb_backups/backup /var/lib/postgresql/${PG_VER}/main"
df -h $df_files 2> /dev/null > /tmp/df.out
rm -f /tmp/high_disk
sort -u /tmp/df.out | grep dev | awk '{print $5}' | while read line ; do
	per=${line/%%/}
	if [ $per -gt 80 ] ; then 
		sudo touch /tmp/high_disk
	fi
done

if [ -f /tmp/high_disk ] ; then
	subject="$subject High Disk Usage;"
	echo "High Disk Usage detected:" >> $CONTENT
	cat /tmp/df.out >> $CONTENT
	echo >> $CONTENT
else
	echo "No disk space alarms, not alerting."
fi

#
# Check for any wal_archive failures (today only).
#
curdate=`date +%Y-%m-%d`
pg_log=/var/log/postgresql/postgresql-${PG_VER}-main.log
fail_text="archive command failed"

if [ -n "$HA_OTHER_NODE" -o "$HOT_BACKUP_ENABLED" == 'Y' ] ; then
	if [ "$HA_IS_STANDBY" != 'Y' -a "$HA_FAILED_STATE" != 'Y' ] ; then
		# archiving is on. Check the health for any failures
		# exclude err code 19 since that is internally generated during rotation
		failures=`grep "$curdate.*$fail_text" $pg_log | grep -c -v "code 19"`
		if [ $failures -gt 100 ] ; then
			subject="$subject WAL-archive Error;"
			echo "WAL Archive Errors detected:" >> $CONTENT
			grep -A2 "$curdate.*$fail_text" $pg_log | grep -v "code 19" | tail -200 >> $CONTENT
			echo >> $CONTENT
		else
			echo "No archive errors, not alerting"
		fi
	fi
fi

#
# Check for any OOM errors (dumpmon.log will be present for the current date)
#
monfile=/var/log/insta/dumpmon.$curdate.log
if [ -f $monfile ] ; then
	subject="$subject tomcat Error;"
	echo "Tomcat restart/Out Of Memory error detected: " >> $CONTENT
	tail -200 $monfile >> $CONTENT
	echo >> $CONTENT
else
	echo "No tomcat errors, not alerting"
fi

#
# Check for any errors in backup process (backup-$curdate.log will exist)
#
bkperrfile=/var/log/insta/backup.$curdate.log
if [ -f $bkperrfile ] ; then
	subject="$subject Backup Error;"
	echo "Backup error detected: " >> $CONTENT
	tail -200 $bkperrfile >> $CONTENT
	echo >> $CONTENT
else
	echo "No backup errors, not alerting"
fi

hotbackup_pid=/var/run/hot_backup.pid
if [ -f $hotbackup_pid ] ; then
	# hotbackup rotation has not completed after 1 day. Bad.
	subject="$subject Hot Backup Error;"
	echo "Hot Backup error detected: " >> $CONTENT
	echo "PID `cat $hotbackup_pid` still not running" >> $CONTENT
	tail -200 /var/log/insta/hms/hot_backup.log >> $CONTENT
else
	echo "No Hot backup errors, not alerting"
fi

if [ ! -z "$HA_OTHER_NODE" ] ; then
	ping -nq -c1 $HA_OTHER_NODE >/dev/null
	if [ $? -ne 0 ] ; then
		subject="$subject HA Reachability error;"
	echo "HA Reachability Error: " >> $CONTENT
	echo "Could not ping $HA_OTHER_NODE" >> $CONTENT
	else
		echo "No HA reachability errors, not alerting"
	fi
fi

#
# If any errors are there, send a mail with the content
#
if [ ! -z "$subject" ] ; then
	echo "Errors exist ($subject), sending mail."
	# errors exist, send the mail
	echo "From: $HOST Health Check <$HOST@hub.instahealthsolutions.com>" > $MESSAGE
	echo "To: $IT_SUPPORT_MAIL_ID" >> $MESSAGE
	echo "Subject: Healthcheck failures on $HOST: $subject" >> $MESSAGE
	echo >> $MESSAGE

	cat $CONTENT >> $MESSAGE

	if [ "$1" != "test" ] ; then
		/usr/sbin/sendmail -t -r $REPLY_TO < $MESSAGE
	fi
else
	echo "No errors, not sending mail"
fi

echo "======================="
