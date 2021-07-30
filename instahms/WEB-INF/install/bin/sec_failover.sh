#!/bin/bash
#
# Initiate a failover on the standby server, in SR environment
#
# failed_host_name=$2
# failed_port=$3
# failed_db_cluster=$4
# new_master_host_name=$7

[ -f /etc/hms/options ] && source /etc/hms/options

#trigger file to stop replication on secondary
trigger=/tmp/pgsql.failover

[ "$HA_IS_STANDBY" != 'Y' ] && echo "Not a standby server, exiting" && exit 1
[ -z $HA_OTHER_NODE ] && echo "Not a standby server (other node not defined), exiting" && exit 1

# Touch the file to stop replication on standby
sudo touch $trigger
sudo chown postgres:postgres $trigger

# Wait till the failover is complete. postgresql will delete the failover
# file once it is done recovering (see recovery.conf)

while [ -f /tmp/pgsql.failover ] ; do
#	echo "Waiting for recovery to complete .."
	sleep 1
done

#echo "Recovery is complete, starting the application on this node"

# Indicate that we are no longer the standby, and that we are in a failed state.

sudo sed --in-place -e '/HA_IS_STANDBY/d' /etc/hms/options

# Indicate that we are in failed state (ie, only one node is up)
# In this state, wal archive goes to disk instead of the other server.
sudo sed --in-place -e '/HA_FAILED_STATE/d' /etc/hms/options
echo "HA_FAILED_STATE=Y" >> /etc/hms/options


