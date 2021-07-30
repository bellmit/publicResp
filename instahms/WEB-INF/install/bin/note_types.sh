#!/bin/bash

source `dirname $0`/functions

LOGFILE=$LOGDIR/note_types.log

echo "Uploading note_types.csv"
mkdir -p /tmp/masters/

cp $BINPATH/note_types.csv /tmp/masters/

run_in_all_schemas $SQLPATH/upload_note_types.sql >> $LOGFILE 2>&1

if [ $? -ne 0 ] ; then
	do_log "Creation of note types (upload_note_types.sql) failed"
else
	do_log "Creation of note types (upload_note_types.sql) successful"
fi
