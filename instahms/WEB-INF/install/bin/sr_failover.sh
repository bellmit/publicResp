#!/bin/bash
# Script that is run when pgpool detects a primary DB node failure
# special values:  %d = node id
#                  %h = host name
#                  %p = port number
#                  %D = database cluster path
#                  %m = new master node id
#                  %M = old master node id
#                  %H = new master node host name
#                  %P = old primary node id
#                  %% = '%' character

# failed_host_name=$2
# failed_port=$3
# failed_db_cluster=$4
# new_master_host_name=$7

[ -f /etc/hms/options ] && source /etc/hms/options

if [ -z "$APPHOME" ] ; then
	APPHOME="/root/webapps"
fi

failed_node_id=$1
new_master_id=$2
old_master_id=$3
old_primary_node_id=$4
new_master_host=$5

trigger=/tmp/pgsql.failover

sc="sudo ssh $new_master_host"
drc="sudo ssh $DR_NODE"

if [ $failed_node_id = $old_primary_node_id ];then      			# master failed

# Let the standby failover as new master
    $sc "$APPHOME/instahms/WEB-INF/install/bin/sec_failover.sh"        # let standby take over

# Let the DR node start replicating from the new master
    $drc "$APPHOME/instahms/WEB-INF/install/bin/dr_failover.sh $new_master_host"

fi
