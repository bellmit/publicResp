#!/bin/bash

#
# Checks whether new versions are available for upgrade, if so, downloads them 
# Does not auto-upgrade, only does the download. Auto-upgrade may be triggered
# by another cron-job auto_upgrade.sh
#
# Will be run as a cron-job, on local servers.
#

source `dirname $0`/functions

if [ -z $RSYNC_MODULE_NAME ] ; then
	HOST=`hostname`
	HOST=${HOST#instahms-}
else
	HOST=$RSYNC_MODULE_NAME
fi

#
# Disable if not a local server.
#
is_local_server || exit 0

#
# Run only for the default app, disable for subsidiary apps, this is because we only
# need one build to be sync'd, we don't need one per app.
#
is_default_app || exit 0

date_echo "---- Starting get_latest_apps.sh -----"
#
# Get down the list of builds available to us
#
cd $HMS_DIST_HOME/Appsbuilds
sudo /usr/bin/rsync -az $HUBVPN::$HOST/appsbuilds.list .
[ $? -ne 0 ] && echo "Unable to get the apps builds list: $?" && exit 1

#
# rsync any builds that we don't have
#
for build in `cat appsbuilds.list` ; do
	# if we already have the build, continue
	[ -e $build ] && continue

	# Sleep for a random amount of time between 0 and 10 minutes, to avoid overload on apps
	if [ "$1" != "immediate" ] ; then
		sleep $((RANDOM*600/32767))
	fi

	# find a suitable old build for making the rsync base to copy over
	prvs=`find . -maxdepth 1 -mindepth 1 -type d -printf '%T@ %p\n' | sort | tail -1 | awk '{print $2}'`
	if [ ! -z $prvs ] ; then
		echo "Copying prvs build $prvs to $build"
		sudo cp -a $prvs $build
	fi

	# do the sync
	sudo /usr/bin/rsync -az --delete $HUBVPN::Appsbuilds/$build .

	# check for success
	if [ $? -ne 0 ] ; then
		# sync failed, clean it up, so that prvs is not used as the "current" build
		echo "Sync failed: removing $build"
		sudo rm -rf $build
		exit 1
	else
		# sync success: cross-check whether the version that we got matches the dir name
		NEWVER=`get_version_of $build`
		if [ $NEWVER == $build ] ; then
			echo $build > newbuild
			echo "Completed syncing build $NEWVER, setting newbuild to this"
		else
			# sync succeeded, but wrong build.
			echo "Incorrect version contents: $NEWVER : Removing $build"
			sudo rm -rf $build
			exit 1
		fi
	fi
done

#
# Remove any builds that we don't need anymore
#
for build in * ; do
	[ $build == 'install' ] && continue;
	[ $build == 'newbuild' ] && continue;
	[ $build == 'appsbuilds.list' ] && continue;

	grep -q $build appsbuilds.list
	if [ $? -ne 0 ] ; then
		echo "Removing old build $build"
		sudo rm -rf $build
	fi
done

