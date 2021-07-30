#!/bin/bash

#
# Twice daily run of store stock checkpoint
# Depending on whether one checkpoint or two checkpoints per day is set, this will give 14 or 7 days of checkpoint data 
#

source `dirname $0`/functions
LOGFILE=$LOGDIR/accounting_export.log

SCHEMA=$1

[ "$HA_IS_STANDBY" == 'Y' ] && exit 0

function usage() {
	echo " Usage : accounting_export.sh <schema-name>"
}

function run_query_in_schema() {
	local schema=$1
	local stmt="$2"
	echo "set search_path to $schema; $stmt; " | psql -t -q --no-psqlrc --no-align
}

function table_exists() {
	local schema=$1
	local table=$2
	local statement="SELECT 1 FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = '$schema' AND c.relname = '$table' AND c.relkind = 'r'; "
	local result=$(run_query_in_schema $schema "$statement")
	echo $result
}

function accounting_interface_installed() {
	local schema=$1
	local table=hms_accounting_info
	local result=$(table_exists $schema $table)
	echo $result
}

[ -z $SCHEMA ] && usage && exit 1
script="select export_accounting_vouchers()"

do_log "Checking if accounting is installed"

interface_installed=$(accounting_interface_installed $SCHEMA)

if [ -z $interface_installed ] ; then
	do_log "Accounting interface not found in schema $SCHEMA, skipping export" && exit 0
elif [ $interface_installed -eq 1 ] ; then
	do_log "Running accounting export in schema : $SCHEMA"
	run_stmt_in_schema $SCHEMA "$script"
	do_log "======== done ========="
# else fall through, we got a value other than 1 from the query (?)
fi

exit 0
