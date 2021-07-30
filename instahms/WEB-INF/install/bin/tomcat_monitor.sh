#!/bin/bash

#
# Monitor for tomcat process. If it is not alive, restart it. If it is out of memory,
# restart it after ensuring that heap dump is complete. This should be called from
# a cron-job, even as frequently as 1 minute is OK. Suggested: 5 minutes.
#

source `dirname $0`/functions

# This is applicable for the entire server, not each app
is_default_app || exit 0

if [ ! -f /var/run/tomcat-9.pid ] ; then
	date_echo "No tomcat pid file, tomcat not running (on purpose), no action"
	exit 0
fi

tpid=`sudo cat /var/run/tomcat-9.pid`
curdate=`date +%Y-%m-%d`
dumpfile=$TOMCAT_HOME/temp/java*.hprof 
sizefile=/tmp/dumpfile_size
monfile=/var/log/insta/dumpmon.$curdate.log

if [ ! -e /proc/$tpid ] ; then
	# process is missing, restart it. Check for file again to avoid race conditions
	# in the case where someone is just about to stop tomcat as in an upgrade.
	sleep 5
	if [ -f /var/run/tomcat-9.pid ] ; then
		# check if port 80 is open and listening
		sudo grep -q 00000000:0050 /proc/net/tcp /proc/net/tcp6
		if [ $? -eq 0 ] ; then
			# strange: pid is missing, but port is open. Report it but do nothing
			date_echo "Missing tomcat: $tpid, but port 80 open (no action)" | tee -a $monfile
			echo >> $monfile
			ps -ef >> $monfile
			echo "=======================" >> $monfile
		else
			# pid missing, port is not open. Really need to restart.
			if [ -f $APPHOME/upgrading ] ; then
				date_echo "Upgrade in progress, no action"
				exit 0
			fi
			date_echo "Missing tomcat process: $tpid, starting tomcat" | tee -a $monfile
			echo >> $monfile
			ps -ef >> $monfile
			echo "=======================" >> $monfile
			sudo /etc/init.d/tomcat-9 start
		fi
	else
		date_echo "No tomcat pid file on second check, tomcat not running (on purpose), no action"
		exit 0
	fi
fi

#
# Check for hprof file under tomcat temp directory. If it exists and has
# stopped growing in size, move it out to another directory and restart tomcat
# If it is still growing, wait till it stops. We could do it in a loop+sleep
# here itself, but the dump may take a long time, and therefore we will need
# to protect ourselves from another instance spawning. Not worth it.
#
if [ -f $dumpfile ] ; then
	cur_size=`stat --printf %s $dumpfile`

	if [ ! -f $sizefile ] ; then
		date_echo "Found new memory dump file $dumpfile (size $cur_size)"
		echo $cur_size > $sizefile
		sudo kill -QUIT `cat /var/run/tomcat-9.pid`
	else
		# we have detected a dumpfile in the previous run
		prvs_size=`cat $sizefile`
		if [ $cur_size -eq $prvs_size ] ; then
			sudo rm $sizefile
			date_echo "Stopped growing, Prvs size: $prvs_size, Cur size: $cur_size. Restarting tomcat."
			sudo mv $dumpfile /var/log/insta/

			# tell our healthcheck monitor that something happened
			date_echo "Found heap dump: $dumpfile (size $cur_size)" >> $monfile
			echo >> $monfile
			top -bn1 >> $monfile
			echo "=======================" >> $monfile

			# restart tomcat
			sudo /etc/init.d/tomcat-9 restart
			echo "======================="

		else
			date_echo "File growing: Prvs size: $prvs_size, Cur size: $cur_size"
			echo $cur_size > $sizefile
		fi
	fi
fi

