#!/bin/bash

#
# part of upgrade: called after the version is switched. Thus, this script
# is run from the "new" version, ie, after it has been installed in the
# final target directory
#
source `dirname $0`/functions
CRON_DB=$PGDATABASE"_q"
NEWSQLPATH=$APPROOT/WEB-INF/install/sql/quartz_tables.sql
UPDATE_QUARTZ_PATH=$APPROOT/WEB-INF/install/sql/update_quartz.sql
echo "Quartz DB : $CRON_DB"

function is_quartz_db_exists() {
        QUARTZ_DB_STATUS=`psql -tAc "SELECT 1 FROM pg_database WHERE datname='$CRON_DB'"`
	if [ "$QUARTZ_DB_STATUS" = "1" ]
	then
		echo 1
	else
		echo 0
	fi
}

function is_schema_exists() {
    psql -q -t --no-psqlrc  <<EOF
	\c $CRON_DB;
	SELECT count(schema_name) FROM information_schema.schemata WHERE schema_name = 'quartz';
EOF
}

function create_quartz_db() {
	echo "Creating database $CRON_DB"
	psql <<EOF
	create database $CRON_DB;
EOF
}

function create_schmea() {
	echo "Creating quartz schema";
	psql <<EOF
	\c $CRON_DB
	create schema quartz;
EOF
}

function create_quartz_tables() {
	echo "Creating quartz tables";
	psql --no-psqlrc --no-align --variable -d $CRON_DB <<EOF
	set search_path to quartz;
	\i $NEWSQLPATH
EOF
}

function update_quartz_tables() {
	echo "Creating quartz tables";
	psql --no-psqlrc --no-align --variable -d $CRON_DB <<EOF
	set search_path to quartz;
	\i $UPDATE_QUARTZ_PATH
EOF
}

function setUp() {
   QUARTZDBEXISTS=`is_quartz_db_exists`
   QUARTZSCHEMAEXISTS=`is_schema_exists`
   if [ "$QUARTZDBEXISTS" -eq "0" ] ; then
           create_quartz_db
           echo "Quartz DB created";
   else
           echo "Quartz db all ready exists";
   fi
   if [ "$QUARTZSCHEMAEXISTS" -eq "1" ] ; then
           echo "Quartz schema all ready exits";
   else
           create_schmea
           create_quartz_tables
           echo "Quartz schema and tables created";
   fi
   echo "Quartz setup done";
}

setUp
update_quartz_tables
