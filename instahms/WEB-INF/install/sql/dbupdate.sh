#!/bin/sh

#
# Script to update a database with db_changes.sql latest changes
# Inputs are: db and schema. Must be run from the WEB-INF/install/sql directory.
#
# This script keeps track of the last time db_changes was applied to the db/schema,
# takes a diff of the current db_changes, and applies all the changed lines. It is
# important, therefore, to ensure that when changining db_changes.sql, you take care of
# the following:
#
# 1. Always APPEND to the end of db_changes.sql all your new sql statements.
#
# 2. If there is an error in any statement, remove it by commenting out all the lines.
#    (do not delete the lines, then the diff will get confused). Then, APPEND at the
#    end of the file, the correct statement.
#
# 3. The rule to follow is that every statement must be added/removed as a whole, ie,
#    if there is a change, change ALL lines in that statement.
#

DB=$1
SCHEMA=$2

DBCHANGES=db_changes_90_91.sql

[ -z $SCHEMA ] && echo "Usage: $0 <db> <schema>" && exit 1
[ ! -f $DBCHANGES ] && echo "Cannot find $DBCHANGES" && exit 1

LASTFILE=.$DB.$SCHEMA.db_changes.sql
DIFFILE=/tmp/db_changes.diff.sql

[ ! -f $LASTFILE ] && echo > $LASTFILE

# get a new diff of previously ran db_changes and the current latest
diff $LASTFILE $DBCHANGES -b \
	--new-line-format='%L' --unchanged-line-format='' --changed-group-format='%>' \
	> $DIFFILE
sudo sed --in-place -e 's/--_INCR_ONLY_ //' $DIFFILE

# save prvs status
mv $LASTFILE /tmp/
cp $DBCHANGES $LASTFILE

# generate sql to drop all auditlog triggers
grep -i "DROP TRIGGER" ./auditlog_triggers.sql > /tmp/drops.sql
grep -i "DROP VIEW" ./vft.sql >> /tmp/drops.sql
grep -i "DROP VIEW" ./report_views.sql >> /tmp/drops.sql

# run the following:
#  drop audit log triggers and views,
#  diff
#  run vft.sql
#  run report_views.sql
#  install audit triggers
echo "Running updates on $DB.$SCHEMA"
psql -q -U postgres -d $DB << EOF 
	SET client_min_messages = error;
	set search_path to $SCHEMA;
	\echo Dropping audit log triggers and views ...
	\i /tmp/drops.sql
	\echo running $DIFFILE ...
	\i $DIFFILE
	\echo running vft.sql ...
	\i vft.sql
	\echo running report_views.sql ...
	\i report_views.sql
	\echo running auditlog_triggers.sql ...
	\i auditlog_triggers.sql
EOF

