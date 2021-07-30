#!/bin/bash

#
# Initiate a failover on the standby server.
#

[ -f /etc/hms/options ] && source /etc/hms/options

if [ -z "$APPHOME" ] ; then
	APPHOME="/root/webapps"
fi


# The only parameter required by this script
new_master_host=$1

[ -z $new_master_host ] && echo "Master host not specified, exiting" && exit 1


# remove standby indicator, if it is already there.
sudo sed --in-place -e '/HA_IS_STANDBY/d' /etc/hms/options
echo "HA_IS_STANDBY=Y" >> /etc/hms/options

# Do some sanity checks and bail out when unsafe, eg, primary is reachable
ping -q -c 1 $new_master_host
if [ $? -ne 0 ] ; then
	echo "$new_master_host is not alive! exiting" && exit 1
fi
# echo "$new_master_host:/var/lib/postgresql/9.1/main/pg_xlog/*history* ./"
# copy the history files from the new master host
sudo rsync -a $new_master_host:/var/lib/postgresql/9.1/main/pg_xlog/*history* /var/lib/postgresql/9.1/main/pg_xlog/

# Modify the recovery.conf to point to the new master on the DR Node
sudo chown -R postgres:postgres /var/lib/postgresql/9.1/main/pg_xlog/*history*
if [ $? -ne 0 ] ; then
	echo "Unable to change ownership on history files, exiting" && exit 1
fi

sudo cp $APPHOME/instahms/WEB-INF/install/recovery.conf.sr /var/lib/postgresql/9.1/main/recovery.conf
if [ $? -ne 0 ] ; then
	echo "Unable to copy recovery config file, exiting" && exit 1
fi

sudo sed --in-place -e s/__HA_OTHER_NODE__/$new_master_host/ /var/lib/postgresql/9.1/main/recovery.conf
if [ $? -ne 0 ] ; then
	echo "Unable to modify recovery.conf, exiting" && exit 1
fi

sudo chown postgres:postgres /var/lib/postgresql/9.1/main/recovery.conf
if [ $? -ne 0 ] ; then
	echo "Unable to change ownership of recovery.conf, continuing recovery..."
fi

# restart Postgresql on DR Node so that it starts replicating from the new master
echo "Restarting postgres in standby mode...."
sudo /etc/init.d/postgresql restart
if [ $? -ne 0 ] ; then
	echo "Unable to restart postgres, please restart postgres manually to complete switchover" && exit 1
fi

echo "Postgres started successfully. Please verify the logs to make sure streaming replication has started."
exit 0

