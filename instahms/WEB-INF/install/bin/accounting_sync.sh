#!/bin/bash

#
# Export accounting data to focus. Called from a cron-job periodically (5 minutes).
# Also cleans up some old logs etc once a day.
#

source `dirname $0`/functions
JAVA=/usr/bin/java
export CLASSPATH=$APPROOT/WEB-INF/classes
if [ -z "$PGUSER" ] ; then
	export PGUSER=postgres
fi
if [ -z "$PGPORT" ] ; then
	export PGPORT=5432
fi

function get_export_prefs() {
	local schema=$1
	PSQLC="psql -d hms -t -A -q --no-psqlrc -c "
	echo `$PSQLC "SELECT directory, target_url, export_type from $schema.scheduled_export_prefs "`;
}

function cleanup_log() {
	local schema=$1
    psql -q --no-psqlrc <<EOF
        SET search_path to $schema;

		DELETE FROM accounting_xml_export_import_log
		WHERE exported_date_time < current_date - 31 AND status in ('Success','Empty');

		DELETE FROM accounting_voucher_details v
		WHERE NOT EXISTS (SELECT export_no
			FROM accounting_xml_export_import_log WHERE export_no = v.export_no);
EOF
}


hod=`date +%-H`
min=`date +%-M`
if [ $hod -eq 1 -a $min -le 5 ] ; then
	cleanup=Y
fi

for schema in `get_all_schemas` ; do
	# get the focus base dir from the DB
	for pref in `get_export_prefs $schema` ; do

		# trim spaces
		dir=`echo $pref | cut -d'|' -f 1`;
		target_url=`echo $pref | cut -d'|' -f 2`;
		export_type=`echo $pref | cut -d'|' -f 3`

		if [ -z $dir ] ; then
			continue
		fi

		# run the focus process.
		# to enable the debug and test with our FocusTestServlet, pass the server url like
		# http://localhost:8080/instahms/FocusSyncTestServlet and isTestCase (last param) as true.
		date_echo "Running in schema $schema, dir: $dir, URL: $target_url"

		if [ "$export_type" == 'focus' ] ; then
			java com.insta.hms.focus.FocusSyncReader $dir/input $dir/"done" $dir/error $target_url false
		else
			java com.insta.hms.tally.TallySyncReader $dir/input $dir/"done" $dir/error $target_url false
		fi

		if [ "$cleanup" = 'Y' ] ; then
			date_echo "Cleaning up old log files more than a month old"
			# cleanup old files which are successful, and more than 1 month old
			find "$dir/done/" -mtime +31 -exec rm '{}' ';'
			# cleanup old logs which are successful, and more than 1 month old
			cleanup_log $schema
		fi
	done
done

