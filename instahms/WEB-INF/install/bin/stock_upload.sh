#!/bin/bash

source `dirname $0`/functions

SCHEMA=$1
DATADIR=$2

[ -z $DATADIR ] && DATADIR=$HMS_WORK_HOME/masters

[ -z $SCHEMA ] && echo "Usage: master_upload.sh <schema> [datadir]" && exit 1

SHEETS="stockdetails"

mkdir -p /tmp/masters
for sheet in $SHEETS ; do
	if [ ! -f $DATADIR/$sheet.csv ] ; then
		echo "Data file $DATADIR/$sheet.csv not found, skipping $sheet"
		continue
	fi

	echo "Uploading $sheet"
	cp $DATADIR/$sheet.csv /tmp/masters

	psql -q <<EOF
		\set ON_ERROR_STOP
		SET search_path TO $SCHEMA;
		\i $SQLPATH/upload_$sheet.sql
EOF
	if [ $? -ne 0 ] ; then
		echo
		echo -n "ERROR uploading $sheet. Do you want to continue [y/n]: "
		read
		if [ $REPLY != 'y' ] ; then
			echo "Exiting ..."
			exit 1
		fi
	fi

done

