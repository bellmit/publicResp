#!/bin/bash

USERNAME="enInsta"
PASSWORD="Lk#edFr45@wer"
SERVER="ftp.enlightiks.com"
FILE="*.csv"
# Directory of CSV dumps
DIR="/tmp/ceodashboard"

if [ ! -d "$DIR" ] || [ ! "$(ls -A $DIR)" ]; then
	echo "$(date +'%a %m-%d-%Y %H:%M:%S'): No dumps. so Skipping"
	exit 1;
fi
cd $DIR
# Upload the files via ftp
ftp -pinv $SERVER << EOF
ascii
user $USERNAME $PASSWORD
prompt
mput $FILE
pwd
bye
EOF
rm $FILE
echo "$(date +'%a %m-%d-%Y %H:%M:%S'): Uploaded files to ftp"

