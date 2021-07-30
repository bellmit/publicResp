#!/bin/bash

#
# part of upgrade: called from postswitch. This will update the custom reports
# metadata based on what we have in our SVN database.
#

source `dirname $0`/functions
LOGFILE=$LOGDIR/upgrade.log

PSQLC="psql -t -A -q --no-psqlrc -c"
schemas=`get_all_schemas`
CUSTOMPATH=$APPROOT/WEB-INF/custom

for schema in $schemas ; do
	reports=`$PSQLC "SELECT report_id, file_name FROM $schema.custom_reports WHERE COALESCE(file_name, '') != ''"`
	for report in $reports ; do
		id=${report%|*}
		filename=${report#*|}

		if [ -f $CUSTOMPATH/$filename ] ; then
			filecontent=`cat $CUSTOMPATH/$filename`
			echo "UPDATE $schema.custom_reports SET report_metadata = :rpt WHERE report_id = $id" | \
				psql -q --no-psqlrc --set rpt="\$\$$filecontent\$\$"
		fi
	done
done

