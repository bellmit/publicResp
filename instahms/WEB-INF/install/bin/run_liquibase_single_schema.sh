#!/bin/bash
if [ $# -ne 2 ] ; then 
	echo "Usage: $0 <DB> <SCHEMA>"
        exit 1
fi
APP=$1
DB=$APP
schema=$2
[ -f /etc/hms/options ] && source /etc/hms/options
[ -f /etc/hms/options.$APP ] && source /etc/hms/options.$APP

if [ -z "$APPHOME" ] ; then
	APPHOME="/root/webapps"
fi

export PGDATABASE=$DB
if [ -z "$PGUSER" ] ; then
	export PGUSER=postgres
fi
if [ -z "$PGPORT" ] ; then
	export PGPORT=5432
fi
export PGHOST=$DBHOST
export PGPASSWORD=$DBPASSWORD
export PGPORT=$DBPORT

if [ -z "$DBHOST" ] ; then
  DBHOST="localhost"
fi

APPROOT="$APPHOME/insta$APP"
echo "Running upgrades for insta$APP via $APPROOT"

function check_for_schema() {
	local SCHEMA=$1
	local STMT="$2"
	psql -q << EOF
	\t
	set search_path to $SCHEMA;
	select '$SCHEMA' WHERE $STMT;
EOF
}

prec3schemas=`check_for_schema $schema "(SELECT after_decimal_digits FROM generic_preferences) = 3"`

echo "Upgrading $schema"
sudo cp $APPROOT/WEB-INF/classes/liquibase/liquibase.update.properties.config $APPROOT/WEB-INF/classes/liquibase/liquibase.update.properties
precision=2
if [[ $prec3schemas == $schema ]] ; then
  precision=3
fi
if [[ $prec3schemas == *" $schema "* ]] ; then
  precision=3
fi
if [[ $prec3schemas == *"$schema "* ]] ; then
  precision=3
fi
if [[ $prec3schemas == *" $schema"* ]] ; then
  precision=3
fi
sudo python3 $APPROOT/upgrade_schema.py $schema $DB $APPROOT $precision
