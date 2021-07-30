#!/bin/bash

source `dirname $0`/functions

LOGFILE=$LOGDIR/growthchart_reference_data.log

PSQLC="psql -t -A -q --no-psqlrc -c"

echo "Uploading growth_chart_reference_data.csv"
mkdir -p /tmp/masters/
cp $BINPATH/growth_chart_reference_data.csv /tmp/masters/

schemas=`check_for_schemas "(select count(*) from growth_chart_reference_data) = (0)"`
for schema in $schemas; do
	do_log "uploading growthchart reference data (upload_growthchart_reference_data.sql) on $schema."
	psql -q <<EOF
		\set ON_ERROR_STOP
		SET search_path TO $schema;
		\i $SQLPATH/upload_growthchart_reference_data.sql
EOF
	if [ $? -ne 0 ] ; then
		do_log "uploading growthchart reference data (upload_growthchart_reference_data.sql) failed on $schema."
	else
		do_log "upload_growthchart_reference_data.sql ran successfully on $schema."
	fi
done
