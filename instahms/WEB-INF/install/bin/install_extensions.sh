#!/bin/bash

source `dirname $0`/functions

function is_schema_exists() {
    psql -q -t --no-psqlrc  <<EOF
	\c $PGDATABASE;
	SELECT count(schema_name) FROM information_schema.schemata WHERE schema_name = 'extensions';
EOF
}

function create_schmea() {
	echo "Creating extensions schema";
	psql <<EOF
	\c $PGDATABASE
	create schema extensions;
EOF
}

function create_fuzzystrmatch_extension() {
	echo "Creating fuzzystrmatch extension";
	psql --no-psqlrc --no-align --variable -d $PGDATABASE <<EOF
	set search_path to extensions;
	CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
	
EOF
}

function create_tablefunc_extension() {
	echo "Creating tablefunc extension";
	psql --no-psqlrc --no-align --variable -d $PGDATABASE <<EOF
	set search_path to extensions;
	CREATE EXTENSION IF NOT EXISTS tablefunc;
EOF
}

function create_btree_gist_extension() {
	echo "Creating btree_gist extension";
	psql --no-psqlrc --no-align --variable -d $PGDATABASE <<EOF
	set search_path to extensions;
	CREATE EXTENSION IF NOT EXISTS btree_gist;
EOF
}

function setUp() {
        EXTENSIONEXISTS=`is_schema_exists`
	if [ "$EXTENSIONEXISTS" == "1" ]
	then
		echo "extensions schema all ready exits";
	else
		create_schmea	
		echo "extensions schema created";
	fi
	create_fuzzystrmatch_extension
	echo "fuzzystrmatch extension created"
	create_tablefunc_extension
	echo "tablefunc extension created"
	create_btree_gist_extension
	echo "btree_gist extension created"
	echo "extensions setup done";
}

setUp
