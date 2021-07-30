#!/bin/bash
if [ $# -ne 3 ] ; then
	echo "Usage: $0 <DB> <schema> <precision>"
        exit 1
fi
APP=$1
SCHEMA=$2
DB=$APP
PRECISION=$3
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

if [ -z "$PGPORT" ] ; then
  PGPORT="5432"
fi

PSQL="psql -d $DB --no-psqlrc"

echo "\\dn" | $PSQL | grep -wq $SCHEMA
if [ $? -eq 0 ] ; then
	echo -n "Schema $SCHEMA in db $DB already exists, do you want to drop it? [Y/N] "
	read
	if [ $REPLY == 'Y' -o $REPLY == 'y' ] ; then
		echo "SET client_min_messages = warning; drop schema $SCHEMA cascade" | $PSQL
	else
		echo "Aborting. Use the following command to drop the db if required:"
		echo " ( echo 'drop schema $SCHEMA cascade' | $PSQL )"
		exit 2
	fi
fi

APPROOT="$APPHOME/insta$APP"
sudo chmod +x $APPROOT/WEB-INF/install/bin/install_extensions.sh
sudo chmod +x $APPROOT/WEB-INF/install/bin/install_quartz.sh

echo "Creating schema extensions if required in db $DB and installing extensions"
$APPROOT/WEB-INF/install/bin/install_extensions.sh
echo "Creating schema $SCHEMA in db $DB for insta$APP via $APPROOT"
sudo cp $APPROOT/WEB-INF/classes/liquibase/liquibase.update.properties.config $APPROOT/WEB-INF/classes/liquibase/liquibase.update.properties
sudo python3 $APPROOT/create-initial-schema.py $SCHEMA $DB $APPROOT $PRECISION
echo "Schema $schema created in $DB"
echo "Creating quartz database"
$APPROOT/WEB-INF/install/bin/install_quartz.sh
echo "All operations complete"
