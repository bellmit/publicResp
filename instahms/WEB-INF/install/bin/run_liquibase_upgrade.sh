#!/bin/bash
if [ $# -ne 1 ] ; then 
	echo "Usage: $0 <DB>"
        exit 1
fi
APP=$1
DB=$APP
[ -f /etc/hms/options ] && source /etc/hms/options
[ -f /etc/hms/options.$APP ] && source /etc/hms/options.$APP

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

if [ -z "$APPHOME" ] ; then
	APPHOME="/root/webapps"
fi

if [ -z "$DBHOST" ] ; then
  DBHOST="localhost"
fi

if [ -z "$PGPORT" ] ; then
  PGPORT="5432"
fi

#
# Get all the schemas in the given database
#
function get_all_schemas() {
	if [ "$HA_IS_STANDBY" != 'Y' ] ; then
		echo "\\t\\dn" | psql -q --no-psqlrc | awk '{print $1}' | \
			grep -v ^pg_ | grep -v information_schema | grep -v ^public | grep -v extensions | grep -v _temp$
	fi
}

function check_for_schemas() {
	local STMT="$1"
	for SCHEMA in `get_all_schemas` ; do
		psql -q << EOF
		\t
		set search_path to $SCHEMA;
		select '$SCHEMA' WHERE $STMT;
EOF
	done
}

APPROOT="$APPHOME/insta$APP"
echo "Running upgrades for insta$APP via $APPROOT"

prec3schemas=`check_for_schemas "(SELECT after_decimal_digits FROM generic_preferences) = 3"`

for schema in `get_all_schemas` ; do
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
done
