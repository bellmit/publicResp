#!/bin/bash

#
# Simple licence check: Licence is one of:
#  (a) Fixed validity date (specified in /etc/hms/options)
#     To force an expiry date, do the following:
#       date --date='2010-08-31 00:00' +%s
#     and put the result as eg, LICENCE_EXPIRES_ON=1283193000 in /etc/hms/options.
#     This will disable the app exactly at the given date/time.
#
#  (b) 30 days from the last day the insta apps server was reachable
#
# Certificate based check is rather useless since if the hacker can get till here,
# he can easily disable the cron-job as well. To do proper cert based validation, this
# check needs to be done within the compiled app itself so that removing the check is not easy.
#
# Possible statuses are:
#
#  OK: More than 7 days left to expire.
#     No external indication of status.
#     Can move to any of the following states as days progress without change in expiry date.
#  WARNING: Between 7 - 1 days left to expire.
#     Warning displayed in system_messages
#     Can move to back OK when expiry date is moved.
#     Can move to DISABLED/REMOVED as days progress without change in expiry date.
#  DISABLED: 0 to 7 days after expiry date
#     All users and modules inactivated
#     Can move to REMOVED if it remains in this state for longer
#     Cannot move backwards to warning/OK even when expiry date changes
#     Manual intervention (activation from backend) required to bring it back to life:
#      1. update u_user set emp_status = 'A' where emp_status = 'X';
#      2. update modules_activated set activation_status = 'Y' where activation_status = 'X';
#      3. echo OK > /var/log/insta/hms/licence_status.txt
#  REMOVED: More than 7 days after expiry
#     App (and therefore this script too) and schema are deleted
#     Needs re-installation to come back to life.
#

source `dirname $0`/functions
LASTREACH_FILE=$LOGDIR/last_reached.txt
STATUS_FILE=$LOGDIR/licence_status.txt

#
# Disable if not a local server: this includes backup/standby servers
#
is_local_server || exit 0
[ "$HA_IS_STANDBY" == 'Y' ] && exit 0

#
# Adds a licence going to expire warning in the system messages. If it already exists,
# then updates the warning with new info (num days etc.)
#
function add_system_warning() {
	local msg=
	if [ -z $LICENCE_EXPIRES_ON ] ; then
		local lastreach_date=`date -d @${lastreach} +"%d-%m-%Y %H:%M"`
		msg="Insta data center has been unreachable since $lastreach_date."
	else
		local expires_date=`date -d @${expires_on} +"%d-%m-%Y %H:%M"`
		msg="Licence expires on $expires_date."
	fi
	msg="$msg Insta HMS will be deactivated in $days_left days"

	local stmt=
	if [ $old_status == 'WARNING' ] ; then
		# update is required since the number of days may be different.
		stmt="UPDATE system_messages SET messages='$msg' WHERE system_type = 'Licence'"
	else
		# insert
		stmt="INSERT INTO system_messages VALUES (nextval('system_messages_seq'),'$msg','Licence','A',0)"
	fi
	run_stmt_in_all_schemas "$stmt"
	refresh_messages
}

#
# Removes the system message warning
#
function remove_system_warning() {
	local remove_warning="DELETE FROM system_messages WHERE system_type = 'Licence'"
	run_stmt_in_all_schemas "$remove_warning"
	refresh_messages
}

function disable_app() {
	local disable_users="UPDATE u_user SET emp_status = 'X' WHERE emp_status = 'A'"
	local disable_modules="UPDATE modules_activated SET activation_status = 'X' WHERE activation_status = 'Y'"
	run_stmt_in_all_schemas "$disable_users"
	run_stmt_in_all_schemas "$disable_modules"
	# no longer require the system warning
	remove_system_warning
}

function remove_app() {
	sudo /etc/init.d/tomcat-9 stop
	# keep a backup of just-before removal

	pg_dump -f /var/backups/pre-remove.dump -Fc hms -U postgres
	[ $? -ne 0 ] && return 1

	# drop the database
	sudo /usr/bin/dropdb ${APP}
	# schedule the following for later execution, since this script itself is
	# going to be removed, so that the command is executed after we exit.
	{ sleep 10 ; sudo rm -rf $APPHOME/insta${APP} ; } &
}

#
# Main starts here.
#
if [ -f $STATUS_FILE ] ; then
	old_status=`cat $STATUS_FILE`
else
	old_status=OK
fi

now=`date +%s`			# seconds since epoch
days=86400				# num seconds in a day = 60*60*24

if [ -z $LICENCE_EXPIRES_ON ] ; then
	result=FAILED
	ping -nq -c1 $HUBVPN >/dev/null && result=SUCCESS

	if [ $result == "SUCCESS" ] ; then
		echo $now > $LASTREACH_FILE
		lastreach=$now
	else
		if [ -f $LASTREACH_FILE ] ; then
			lastattempt=`stat -c %Y $LASTREACH_FILE`
			file_age=$((now-lastattempt))
			# don't trust if last attempt was more than 10 days. Can happen if there
			# is a remnant of this file, or if server was down for some time. Clock resets in this case.
			[ $file_age -gt $((10*days)) ] && sudo rm $LASTREACH_FILE
		fi

		if [ -f $LASTREACH_FILE ] ; then
			lastreach=`cat $LASTREACH_FILE`
		else
			# assume it was reachable 10 days back.
			lastreach=$((now-days*10))
			echo $lastreach > $LASTREACH_FILE
		fi
		# record that we attempted by touching the file: mtime stores last attempt time
		sudo touch $LASTREACH_FILE
	fi
	expires_on=$((lastreach+(30*days)))
else
	expires_on=$LICENCE_EXPIRES_ON
fi

time_left=$((expires_on-now))
days_left=$((time_left/days))		# truncates, even 1/2 day becomes 0. Use only for display

#
# Figure out the new status: backward state changes are not always permitted
#
if [ $time_left -gt $((7*days)) ] ; then
	# More than 7 days to expire
	if [ $old_status != 'DISABLED' -a $old_status != 'REMOVED' ] ; then
		# warning to ok is fine
		new_status=OK
	else
		new_status=$old_status
	fi

elif [ $time_left -gt 0 ] ; then
	# More than 0 days left to expire, but less than 7
	if [ $old_status != 'DISABLED' -a $old_status != 'REMOVED' ] ; then
		new_status=WARNING
	else
		new_status=$old_status
	fi

elif [ $time_left -gt $((-7*days)) ] ; then
	# Already expired, but within the last 7 days
	if [ $old_status != 'REMOVED' ] ; then
		new_status=DISABLED
	else
		new_status=$old_status
	fi
else
	# Already expired, for more than 7 days
	new_status=REMOVED
fi

if [ $new_status != 'OK' -o $old_status != 'OK' ] ; then
	date_echo "Old: $old_status, new: $new_status, time left $expires_on-$now=$time_left ($days_left days)"
fi

#
# In Warning state, we keep updating the message, since the number of days
# left to expire can change.
#
if [ $new_status == 'WARNING' ] ; then
	date_echo "Adding warning: expiring in $days_left days"
	add_system_warning
fi

# 
# Handle other state changes
#
if [ $old_status == 'WARNING' -a $new_status == 'OK' ] ; then
	# remove the warning
	date_echo "New status is OK, removing warning"
	remove_system_warning

elif [ $old_status != 'DISABLED' -a $new_status == 'DISABLED' ] ; then
	date_echo "Expired, disabling app: days_left=$days_left"
	disable_app

elif [ $new_status == 'REMOVED' ] ; then
	if [ $old_status == 'OK' ] ; then 
		# we can't suddenly go from OK to removed unless we have been just
		# switched on. Otherwise, it could be a typo in the expires value. Don't take action.
		uptime=`sudo cat /proc/uptime`		# eg 21612345.55 21355817.93
		uptime=${uptime%%.*}			# remove everything after first decimal
		updays=$((uptime/86400))
		if [ $updays -gt 7 ] ; then
			date_echo "Sudden switch to REMOVED (updays: $updays, old status: $old_status): NOT removing app"
			exit 1
		else
			date_echo "OK to REMOVED (updays: $updays, old status: $old_status): removing app"
		fi
	fi
	date_echo "Expired for long, removing app: days_left=$days_left"
	remove_app
fi

# Save the new status for next iteration
echo $new_status > $STATUS_FILE

