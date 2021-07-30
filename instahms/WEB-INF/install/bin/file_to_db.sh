#!/bin/bash

#
# Uploads a file into the db via command line, given the file name as
# well as the query to identify the table and row. For example
#
#  file_to_db.sh  hmsnew  migrated  form.pdf "UPDATE my_table SET uploaded_file=?"
#

db=$1
schema=$2
file=$3
stmt=$4

[ -z "$4" ] && echo "Insufficient arguments. Usage: file_to_db.sh <db> <schema> <file> \"<sqlstmt>\"" && exit 1
[ ! -z "$5" ] && echo "Too many arguments. Usage: file_to_db.sh <db> <schema> <file> \"<sqlstmt>\"" && exit 1
[ ! -f "$file" ] && echo "File not found: $file" && exit 1

newstmt="${stmt/\?/decode(:content,'base64')}"
echo "Statement to be executed is: $newstmt"

psql --no-psqlrc -d $db << EOF
	set search_path to $schema;
	\echo setting content
	\set content '\'' \`base64 ${file}\` '\''
	\echo executing statement
	$newstmt;
EOF

