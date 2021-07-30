#!/bin/bash

#
# part of upgrade: called from postswitch. This will update the custom views
# based on what we have in our SVN database.
#

source `dirname $0`/functions
LOGFILE=$LOGDIR/upgrade.log

PSQLC="psql -t -A -q --no-psqlrc -c"
schemas=`get_all_schemas`
CUSTOMPATH=$APPROOT/WEB-INF/custom

echo " schemas are $schemas"
for schema in $schemas ; do
	view_files=`$PSQLC "SELECT file_name FROM $schema.custom_views WHERE COALESCE(file_name, '') != ''"`
	echo "view files are $view_files"
	for view_file in $view_files ; do
		filename=${view_file%|*}
		echo "custom view is $filename"

		if [ -f $CUSTOMPATH/$filename ] ; then
			`run_in_schema $schema $CUSTOMPATH/$filename >> $LOGDIR/migrate.log 2>&1`
		fi
	done
done

