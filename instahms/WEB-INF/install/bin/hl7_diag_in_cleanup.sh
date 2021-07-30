#! /bin/bash

DB=$1
SCHEMA=$2
HOSTNAME=$3


echo "`date +%x::%X` *************INIT*****************"
echo "DB=$DB SCHEMA=$SCHEMA HOSTNAME=$HOSTNAME"
[ -z "$DB" ] && echo "ERROR:: Please specify the database name in 1st argument" exit 1;
[ -z "$SCHEMA" ] && echo "ERROR :: Please pspecify the schema name in 2nd argument" exit 1;
[ -z "$HOSTNAME" ] && echo "Host is missing in 3rd argument, considering localhost"



psql -q --no-psqlrc -h "$HOSTNAME" -U postgres -d "$DB" <<EOF

	set search_path to $SCHEMA;

	WITH output AS (delete from integ_diag_in where status = 'S' RETURNING *) INSERT INTO integ_diag_in_bkp select * from output;

EOF

echo "`date +%x::%X` *************END*****************"
