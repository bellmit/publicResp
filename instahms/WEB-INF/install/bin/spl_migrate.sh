#!/bin/bash

#
# part of upgrade: run for any migrations that cannot be handled by plain sql.
# This is run from the "new" version, ie, after it has been installed in the
# final target directory. Arguments are OLDVER.
#

source `dirname $0`/functions

LOGFILE=$LOGDIR/spl_migrate.log

OLDVER=$1

PSQLC="psql -t -A -q --no-psqlrc -c"

OLDVERN=`get_numeric_version $OLDVER`
OLDMAJOR=$((OLDVERN/100))

CURVER=`get_current_version`
CURVERN=`get_numeric_version $CURVER`
CURMAJOR=$((CURVERN/100))

if [ $OLDMAJOR -le 803 ] ; then
	for schema in `get_all_schemas` ; do
		`$PSQLC "UPDATE $schema.scheduled_export_prefs SET target_url='$FOCUS_SERVER_URL' WHERE COALESCE(target_url, '')='' " `
	done
fi

#
# Run upload_growthchart_reference_data.sql -- after 10.3 version and if growth_chart_reference_data table is empty
#
if [ $OLDMAJOR -ge 1003 ] ; then
	$BINPATH/growthchart_reference_data.sh
fi

#
# Run accounting migration for missed out tax vouchers in the first release of 11.11.0
#

if [ $OLDVERN -ge 111100 -a $OLDVERN -le 111102 ] ; then
    stmt="SELECT post_missing_tax_accounting_vouchers();"
    run_stmt_in_all_schemas "$stmt" "Updating missing tax vouchers: "
fi

#
# Run Custom section creation from 12.0
#
if [ $CURMAJOR -ge 1200 ]; then
	$BINPATH/custom_sections.sh
fi

#
# Run Note types from 12.2
#
if [ $CURMAJOR -ge 1202 ]; then
	$BINPATH/note_types.sh
fi
