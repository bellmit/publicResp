#
# ha recovery conf file
#
# Use pg_standby (install postgresql-contrib) for warm standby.
# -t parameter says which file needs to be present for failover: "touch /tmp/pgsql.failover" to failover
# -s 60 indicates check for new WAL files this frequently. 
# %r inidicates the earliest file we need to keep for safe recover.
#   pg_standby can handle %r to clean up files we don't need, but we want to keep them forever for PITR
#   We manage it ourselves by saving it in last_redo_file for rotating backups.
# 
# Important to note that the pg_standby command has to be the last, since returning an error is important
# for failover process.
#

restore_command = 'echo %r > /var/lib/postgresql/__pg_ver__/ha_backups/backup/last_redo_file ; sudo /usr/lib/postgresql/__pg_ver__/bin/pg_standby -s 60 -t /tmp/pgsql.failover /var/lib/postgresql/__pg_ver__/ha_backups/backup/wal %f %p 2>>/tmp/standby.log '

# At the end of recovery, remove the failover indicator
recovery_end_command = 'sudo rm -f /tmp/pgsql.failover'

