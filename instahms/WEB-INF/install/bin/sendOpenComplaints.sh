#!/bin/bash

#
# Runs only on apps.
#
# Consolidates all open complaints from all servers (including self), and sends
# it to customer-support. Must be run only once a day, after getOpenComplaints.sh
#

source `dirname $0`/functions

# can set in prefs
[ -z $CL_RECIPIENTS ] && CL_RECIPIENTS="insta-support@practo.com"

ALL_COMPS=$HMS_WORK_HOME/usage_logs/all_complaints.csv

# we run only on apps, only for the default app
[ $HOST != 'apps.instahealthsolutions.com' ] && exit 0;
is_default_app || exit 0

#
# Write the csv header
#
echo "Schema,Version,ID,Logged Date,Module,Summary,Description,Assigned To,Status,Complaint Type" > $ALL_COMPS

#
# Fetch our complaints (apps schemas)
#
if [ -f /tmp/open_complaints.csv ] ; then
	cat /tmp/open_complaints.csv >> $ALL_COMPS
fi

#
# Fetch all complaints from remote servers
#
for server in $HMS_WORK_HOME/remote-backups/* ; do
	if [ -f $server/open_complaints.csv ] ; then
		cat $server/open_complaints.csv >> $ALL_COMPS
		# remove it: we don't want to send the same set again if server couldn't update it
		sudo rm $server/open_complaints.csv
	fi
done

#
# Send mail with the csv file as an attachment
# (remove lines with \r: these are "enter"s in the description, should not add to the count.)
#
lines=`sudo sed -e '/\r$/d' $ALL_COMPS | wc -l`
lines=$((lines-1))

if [ $lines -gt 0 ] ; then
	echo "Attached is the open complaints as of `date`" | mutt \
		-a $HMS_WORK_HOME/usage_logs/usage_logs/all_complaints.csv -s "Open complaints: $lines" -- $CL_RECIPIENTS
else
	echo "No open complaints as of `date`" | mutt -s "Open complaints: none" -- $CL_RECIPIENTS
fi

