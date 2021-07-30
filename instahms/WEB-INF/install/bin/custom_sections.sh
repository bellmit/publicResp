#!/bin/bash

source `dirname $0`/functions

LOGFILE=$LOGDIR/custom_section.log

echo "Uploading custom_section.csv"
mkdir -p /tmp/masters/

cp $BINPATH/custom_sections.csv /tmp/masters/

run_in_all_schemas $SQLPATH/upload_custom_sections.sql >> $LOGFILE 2>&1

if [ $? -ne 0 ] ; then
	do_log "Creation of custom sections (upload_custom_sections.sql) failed"
else
	do_log "Creation of custom sections (upload_custom_sections.sql) successful"
fi
