#!/bin/bash

source `dirname $0`/functions
source /etc/hms/options
[ -z "$IT_SUPPORT_MAIL_ID" ] && IT_SUPPORT_MAIL_ID='insta-devops@practo.com'

REPLY_TO="root@hub.instahealthsolutions.com"
CONTENT="/tmp/streamingdelay.out"
MESSAGE="/tmp/streamingdelay.msg"
echo > $CONTENT
wal_check=`sudo grep  'wal_level' /etc/postgresql/9.3/main/postgresql.conf  | awk '{print $3}'`

if [ "$HA_IS_STANDBY" = 'Y' ] ; then

if [ $wal_check == "hot_standby" ]; then

    psql -c  "select now()-pg_last_xact_replay_timestamp() as replication_lag" -o "/tmp/streamingdelay.out"

        echo "From: $HOST Health Check <$HOST@hub.instahealthsolutions.com>" > $MESSAGE
        echo "To: $IT_SUPPORT_MAIL_ID" >> $MESSAGE
        echo "Subject: Streaming status on $HOST: $subject" >> $MESSAGE
        echo >> $MESSAGE
        cat $CONTENT >> $MESSAGE

        sudo  /usr/sbin/sendmail -t -r $REPLY_TO < $MESSAGE

else
    echo "Streaming replication is not enabled here"
    exit 0;
fi
fi 
