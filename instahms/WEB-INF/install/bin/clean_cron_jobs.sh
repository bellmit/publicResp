#!/bin/bash

source `dirname $0`/functions
echo "Quartz db : ${PGDATABASE}_q";

if [ -z "$1" ]
then
	echo "No schema name supplied. Ex: ./clean_cron_jobs str"
	exit 1
else
	echo "Schema Name: $1"
	psql -d "${PGDATABASE}_q" <<EOF
		set search_path to quartz;
		delete from qrtz_cron_triggers where trigger_group ilike '$1_%';
		delete from qrtz_simple_triggers where trigger_group ilike '$1_%';
		delete from qrtz_triggers where trigger_group ilike '$1_%';
		delete from qrtz_job_details where job_group ilike '$1_%';
EOF
fi

