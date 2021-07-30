#!/bin/bash

# this script insert data only for Nationality, Department, 
# Discharge Types, Marital Status, Religion.

source `dirname $0`/functions

LOGFILE=$LOGDIR/malaffi_dataset.log
schema=$1
echo "Uploading malaffi_dataset"
mkdir -p /tmp/masters/

cp -r $BINPATH/malaffi_dataset /tmp/malaffi_dataset

run_in_schema $schema $SQLPATH/malaffi_data_insertion.sql >> $LOGFILE 2>&1

if [ $? -ne 0 ] ; then
	do_log "Creation of Malaffi code sets (malaffi_data_insertion.sql) in schema: "$schema" failed"
else
	do_log "Creation of Malaffi code sets (malaffi_data_insertion.sql) in schema: "$schema" successful"
fi
