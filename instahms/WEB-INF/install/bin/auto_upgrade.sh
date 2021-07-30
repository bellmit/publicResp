#!/bin/bash

#
# Checks whether a new build has been downloaded, and if the preferences are to
# auto-upgrade, then runs upgrade. Note that this is run in the FROM version.
#
# Will be run as a cron-job, on all servers at 4:00 am or so when usage is minimal.
#

HOST=`hostname`
HOST=${HOST#instahms-}
source `dirname $0`/functions

#
# Do nothing if there is no new build
#
BUILDFILE=$HMS_DIST_HOME/builds/newbuild.$APP
UPGRADEALL=N
if [ ! -f $BUILDFILE ] ; then
	# try a generic (for everyone) build file
	BUILDFILE=$HMS_DIST_HOME/builds/newbuild
	UPGRADEALL=Y
fi

if [ ! -f $BUILDFILE ] ; then
	date_echo "No new build, exiting."
	exit 0
fi

#
# do nothing if auto-upgrade is disabled in this server
#
if [ "$AUTO_UPGRADE_ENABLED" == "N" ] ; then
	date_echo "Auto upgrade is disabled on the server, exiting."
	exit 0
fi

NEWVER=`cat $BUILDFILE`
CURVER=`get_current_version`

if [ ! -d $HMS_DIST_HOME/builds/$NEWVER ] ; then
	date_echo "ERROR: Build not found: $HMS_DIST_HOME/builds/$NEWVER"
	exit 1
fi

if [ "$NEWVER" == "$CURVER" ] ; then
	date_echo "WARNING: Versions are same $NEWVER and $CURVER: nothing to do"
	exit 1
fi

NEWVERN=`get_numeric_version $NEWVER`
CURVERN=`get_numeric_version $CURVER`

if [ $NEWVERN -lt $CURVERN ] ; then
	# downgrade, disallow
	date_echo "WARNING: New version is older than current version, not upgrading"
	exit 1
fi

if [ "$AUTO_UPGRADE_MAJOR" == "N" ] ; then
	# ensure that only minor versions differ, otherwise don't upgrade
	# Cannot compare versions directly -> 60405 - 60500 is less than 100. 
	# We need to compare only the portion upto major, so divide by 100
	NEWMAJOR=$((NEWVERN/100))
	CURMAJOR=$((CURVERN/100))
	if [ $NEWMOJOR -ne $CURMAJOR ] ; then
		date_echo "Major versions differ ($NEWMAJOR != $CURMAJOR), and upgrade major is disabled." \
			"Not upgrading"
		exit 1
	fi
fi

# min upgrade version is moved to upgrade itself.

#
# All checks OK, continue to upgrade after sleeping a fixed amount of time (to allow
# for other applications to upgrade)
#
if [ ! -z $CRONDELAY ] ; then
	sleep $CRONDELAY
fi

date_echo "Initiating upgrade: running $BINPATH/upgrade"
echo "to $NEWVER" > $APPHOME/upgrading
# remove the new build indicator, (except if upgrading all, leave it for others to find too)
[ $UPGRADEALL == "N" ] && sudo rm -f $BUILDFILE

# Use exec to handover to upgrade: exec does not return, and upgrade is expected
# to replace this script with a new one.
exec $BINPATH/upgradeInsta $NEWVER

