#!/bin/bash

#
# Periodically sync encounter data exported by Insta to a remote server.
# /etc/hms/options variables used
#   ENCOUNTER_EXPORT_SERVER
#   ENCOUNTER_EXPORT_DIR
# The sftp parameters are to be set up in ~/.ssh/config (IdentityFile, Port, User)
#

source `dirname $0`/functions

mkdir -p /tmp/EncounterData/done
cd /tmp/EncounterData/in

for file in * ; do 
	sftp -b - $ENCOUNTER_EXPORT_SERVER <<<"
		cd $ENCOUNTER_EXPORT_DIR
		put $file
		bye"
	if [ $? -eq 0 ] ; then
		mv $file ../done
	else
		echo "Error sending file to $ENCOUNTER_EXPORT_SERVER"
	fi
done

