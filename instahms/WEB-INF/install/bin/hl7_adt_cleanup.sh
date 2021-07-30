#!/bin/bash

#
# Cleanup of HL7 files/records: called as a cron-job daily.
#

source `dirname $0`/functions

[ -z $HL7_ADT_CLEANUP_DAYS ] && HL7_ADT_CLEANUP_DAYS=3

#
# Get the directory for exports and imports
#

function get_adt_export_dir() {
	local schema=$1
	echo "\t \\\\ select adt_send_filename from $schema.hosp_hl7_prefs WHERE adt_send_type IN ('S') " \
		| psql -q --no-psqlrc
}

schemas=`check_for_schemas '(select count(*) from hosp_hl7_prefs) > 0'`

for schema in $schemas ; do
	# clean up export files
	paths=`get_adt_export_dir $schema`
	for path in $paths ; do
		# knock off the last path element
		dir=${path%/*}
		# delete all files older than 3 days from the export folder
		find "$dir/../in/" -type f -mtime +$HL7_ADT_CLEANUP_DAYS -exec rm '{}' ';'
	done
done
