#!/bin/bash

#
# Initiate a failover on the standby server.
#

source `dirname $0`/functions

[ "$HA_IS_STANDBY" != 'Y' ] && echo "Not a standby server, exiting" && exit 1
[ -z $HA_OTHER_NODE ] && echo "Not a standby server (other node not defined), exiting" && exit 1

# Do some sanity checks and bail out when unsafe, eg, primary is reachable
ping -q -c 1 $HA_OTHER_NODE
if [ $? -eq 0 ] ; then
	echo "Other node is alive!!"
	echo "Failing over can be dangerous, and should be done only for testing."
	echo -n "Are you sure you want to failover? [y/n]: "
	read
	[ $REPLY != "y" ] && echo "Not confirmed, exiting" && exit 1
fi

# Create the failover indicator for postgres
sudo touch /tmp/pgsql.failover
sudo chown postgres:postgres /tmp/pgsql.failover

# Wait till the failover is complete. postgresql will delete the failover
# file once it is done recovering (see recovery.conf)
while [ -f /tmp/pgsql.failover ] ; do
	echo "Waiting for recovery to complete .."
	sleep 5
done
echo "Recovery is complete, starting the application on this node"

# Indicate that we are no longer the standby, and that we are in a failed state.
sudo sed --in-place -e '/HA_IS_STANDBY/d' /etc/hms/options

# Indicate that we are in failed state (ie, only one node is up)
# In this state, wal archive goes to disk instead of the other server.
echo "HA_FAILED_STATE=Y" >> /etc/hms/options

sudo /etc/init.d/tomcat-9 start

echo "Application restart completed. Backup will be stored on disk only."
echo "After the other node is brought up, configure it as the stand by, and run ha_init here."

