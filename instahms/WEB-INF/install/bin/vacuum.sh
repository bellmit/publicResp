#!/bin/bash

#
# Periodically vaccum analyze the database (during lean periods of usage). This is also
# done right after an upgrade, so we need to check for conflicts with upgrade.
#

source `dirname $0`/functions

# Disable if not the default app: since we use --all, we don't need to run on other apps
is_default_app || exit 0

if [ -f $APPHOME/upgrading ] ; then
	date_echo "Periodic vacuum: upgrade in progress, skpping vacuum"
	exit 0
fi

# vacuum and analyze the database indicated by PGDATABASE
vacuumdb --all --analyze

