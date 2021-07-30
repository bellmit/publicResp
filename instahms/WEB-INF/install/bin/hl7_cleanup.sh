#!/bin/bash

#
# Cleanup of HL7 files/records: called as a cron-job daily.
#

source `dirname $0`/functions

[ -z $HL7_CLEANUP_DAYS ] && HL7_CLEANUP_DAYS=31

#
# Get the directory for exports and imports
#

function get_export_dir() {
	local schema=$1
	echo "\t \\\\ SELECT orders_export_dir
					FROM hl7_lab_interfaces hli
					JOIN hl7_center_interfaces hci ON (hli.interface_name = hci.interface_name)
					WHERE export_type IN ('F','B') " \
		| psql -q --no-psqlrc
}

function get_import_dir() {
	local schema=$1
	echo "\t \\\\ select results_import_dir from $schema.hl7_lab_interfaces" | psql -q --no-psqlrc
}

schemas=`check_for_schemas '(select count(*) from hl7_lab_interfaces) > 0'`

for schema in $schemas ; do

	# clean up done export files
	dirs=`get_export_dir $schema`
	for dir in $dirs ; do
		# export dir ends in /in, need to look at done folder
		find "$dir/../done/" -type f -mtime +$HL7_CLEANUP_DAYS -exec rm '{}' ';'
	done

	# clean up done import files
	dirs=`get_import_dir $schema`
	for dir in $dirs ; do
		find "$dir/done/" -type f -mtime +$HL7_CLEANUP_DAYS -exec rm '{}' ';'
		if [ -f "$dir/files" ] ; then
			find "$dir/files/" -type f -mtime +$HL7_CLEANUP_DAYS -exec rm '{}' ';'
		fi
	done

	# clean up the hl7_export_items table itself
	psql -q --no-psqlrc <<EOF
		SET search_path TO $schema;
		DELETE FROM hl7_export_items WHERE export_status = 'S' and exported_ts::date < current_date - $HL7_CLEANUP_DAYS;
EOF
done
