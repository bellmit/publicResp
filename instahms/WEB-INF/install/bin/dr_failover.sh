#!/bin/bash
#
# Initiate a switchover to the new master, on the DR node in SR environment
#
# failed_host_name=$2
# failed_port=$3
# failed_db_cluster=$4
# new_master_host_name=$7

# failed_host_name=$2
# failed_port=$3
# failed_db_cluster=$4
# new_master_host_name=$7

[ -f /etc/hms/options ] && source /etc/hms/options

if [ -z "$APPHOME" ] ; then
	APPHOME="/root/webapps"
fi

#The only parameter required by this script, indicating the new master to replicate from

new_master_host=$1
trigger=/tmp/pgsql.failover

[ "$HA_IS_STANDBY" != 'Y' ] && echo "Not a standby server, exiting" && exit 1
[ "$HA_IS_DRNODE" != 'Y' ] && echo "Not a DR node, exiting" && exit 1

[ -z $new_master_host ] && echo "New master host not specified, exiting" && exit 1

ping -q -c 1 $new_master_host
if [ $? -ne 0 ] ; then
	echo "WARN : $new_master_host is not alive! but continuing with switchover...."
fi

# copy the hostory files from the new master host
sudo rsync -a $new_master_host:/var/lib/postgresql/9.1/main/pg_xlog/*history* /var/lib/postgresql/9.1/main/pg_xlog/

# Modify the recovery.conf to point to the new master on the DR Node
sudo chown -R postgres:postgres /var/lib/postgresql/9.1/main/pg_xlog/*history*
sudo cp $APPHOME/instahms/WEB-INF/install/recovery.conf.sr /var/lib/postgresql/9.1/main/recovery.conf
sudo sed --in-place -e s/__HA_OTHER_NODE__/$new_master_host/g /var/lib/postgresql/9.1/main/recovery.conf
sudo chown postgres:postgres /var/lib/postgresql/9.1/main/recovery.conf

# restart Postgresql on DR Node so that it starts replicating from the new master
sudo /etc/init.d/postgresql restart

