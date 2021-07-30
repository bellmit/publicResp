#!/bin/bash

#
# This script fetches the Open complaints and pushes them to apps.instahealthsolutions.com
# where it gets consolidated and sent out as a single email.
#
# As long as a complaint is Open, it means no action has been taken on it. So, it will
# keep sending the same complaint till someone moves it to Pending or any other status
#
source `dirname $0`/functions

[ "$HA_IS_STANDBY" == 'Y' ] && exit 0
is_default_app || exit 0

SCHEMAS=`get_live_schemas`
VER=`get_current_version`

rm -f /tmp/open_complaints.csv

for schema in $SCHEMAS ; do
	psql -q <<EOF
		SET search_path TO $schema;
		COPY (
			SELECT '$schema', '$VER', complaint_id, logged_date, complaint_module, complaint_summary,
				complaint_desc,'RemoteSupport','Open','Complaint Log', 
				current_date - date(logged_date) as age
			FROM complaintslog
			WHERE complaint_status='Open'
			ORDER BY complaint_id
		) TO '/tmp/schema_open_complaints.csv' csv;
EOF
	# collect all schema's complaints into one one consolidated csv for the server
	# (this is really useful only in apps. On local servers, there will be only one schema)
	cat /tmp/schema_open_complaints.csv >> /tmp/open_complaints.csv
done

# For apps, nothing more to do.
is_local_server || exit 0;

# For local servers, send it to apps for consolidation
sudo rsync -az /tmp/open_complaints.csv $HUBVPN::$HOST/

