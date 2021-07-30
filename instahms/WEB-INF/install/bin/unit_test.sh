#!/bin/bash

APPROOT=$1

DB=hms
schema=test_schema

echo "DROP SCHEMA IF exists $schema"|psql -U postgres -d $DB
sh $APPROOT/WEB-INF/install/bin/install_extensions.sh
cp $APPROOT/WEB-INF/classes/liquibase/liquibase.update.properties.config $APPROOT/WEB-INF/classes/liquibase/liquibase.update.properties
python3 $APPROOT/create-initial-schema.py $schema ${DB} $APPROOT
