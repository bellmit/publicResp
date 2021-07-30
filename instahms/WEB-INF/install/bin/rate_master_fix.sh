#!/bin/bash

source `dirname $0`/functions

#
# Periodically fix the rate master, to work around bugs
# that introduce missing charges.
#

[ "$HA_IS_STANDBY" == 'Y' ] && exit 0

run_in_all_schemas $SQLPATH/rate_master_fix.sql

