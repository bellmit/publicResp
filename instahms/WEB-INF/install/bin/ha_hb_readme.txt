                       HA/HB setup and recovery instructions
                               ==================

Hot Backup
==========
Hot backup mode saves base backups and WAL files on a secondary disk.
Periodically (cron-job based) the set (base+WAL files) is rotated, and N number
of older sets are kept depending on configuration.

Backup Location
---------------
Backups are kept in /var/lib/postgresql/8.4/hb_backups. (8.4 will be replaced
with actual postgresql version).

There is one directory for each set (backup, backup.1, backup.2 ...). Under
each set, there is a data and a wal directory.

backup is the current directory, under which the WAL files are currently being
accumulated. For every rotation backup.1 is moved to backup.2 etc, backup is
moved to backup.1 and a new directory "backup" is initialized.

Setup
-----
1. The secondary disk must be mounted and a directory (or all of it) under that
can be soft-linked as hb_backups under /var/lib/postgresql/8.4.

2. Settings in /etc/hms/options

   HOT_BACKUP_ENABLED=Y
   BACKUP_TYPE=partial
   CLOUDBKP_ENABLED=N

Of the above, CLOUDBKP_ENABLED=N should be setup after confirming that the
customer is following a regular manual disaster backup process.

3. Enable archive mode in postgresql.conf (postgres reload required):

  archive_mode = on
  archive_command = '/var/lib/postgresql/8.4/wal_archive.sh %p %f'
  archive_timeout = 300
  wal_level = archive		# only for postgresql 9.1 and above

Initialization
--------------
Run hot_backup.sh once to save the initial base, while postgres is running.
This will create the initial backup directory (or rotate it, if it is already
there). hot_backup.sh will also be run once a week via cron-job to rotate the
backups.

This can be run multiple times in case of error.

Space usage estimate
--------------------
1. WAL files: at 15 minutes archive timeout, this works out to 64Mb per hour,
which is 1.5G per day, that is 10G per week. At 5 minutes, this is 30G.

2. Data files: as big as the main directory.

For a normal installation of say 20G database and 5 minute archive timeout
and 1 saved backup, this amounts to 20G+30G for 1 week, and total backup
size is 100G.

Recovery
--------
(Change 8.4 to 8.3 wherever applicable)

1. Partial data from original disk is useless. Only use the backup data.
If recovering from corruption, and the main disk is still available, save
the entire contents of /var/lib/postgresql/8.4/main somewhere safe.

2. On the new hardware, Install the OS, application, postgres etc. Connect
the secondary disk to the new hardware.

3. Mount the secondary disk to the new hardware, and create a soft link
from /var/lib/postgresql/8.4/hb_backups to the new disk mount point.

4. Stop postgresql:
     /etc/init.d/postgresql-8.4 stop

5. Remove all directories from /var/lib/postgresql/8.4/main:
     rm -rf /var/lib/postgresql/8.4/main/*

6. Copy the backup data to the main directory:
     cp -a /var/lib/postgresql/8.4/hb_backups/backup/data/* \
	   /var/lib/postgresql/8.4/main/

Note: some older versions of postgresql did not have server.crt and server.key
files under main. If this is the case, and postgres gives an error during
startup, create these softlinks under the main directory:

    server.crt -> /etc/ssl/certs/ssl-cert-snakeoil.pem
    server.key -> /etc/ssl/private/ssl-cert-snakeoil.key


7. Copy recovery.conf.hb into the main directory and change ownership:
     cp /root/webapps/instahms/WEB-INF/install/recovery.conf.hb \
        /var/lib/postgresql/8.4/main/recovery.conf
     chown postgres:postgres /var/lib/postgresql/8.4/main/recovery.conf

8. Edit recovery.conf and uncomment the appropriate lines depending on 
postgres version.

9. Create the directory archive_status under pg_xlog and change ownership:
     mkdir /var/lib/postgresql/8.4/main/pg_xlog/archive_status
     chown postgres:postgres /var/lib/postgresql/8.4/main/pg_xlog/archive_status

10. Start postgres, watch the logs for error conditions.

11. If all is successful, login using psql and check the db contents.
Initialize hb_backup. Monitor the postgresql log file for at least 15
minutes, also ensure log files are being copied into the wal directory
in the backup area.

PITR Recovery
-------------
An additional setting in recovery.conf specifying the time till which to
recover will recover the data till that point in time. Example entry in
recovery.conf:

recovery_target_time = '2013-06-24 15:06:00

High Availability Pair
======================
Primary's responsibility is to continuously ship the WAL logs to the secondary.
On the secondary, the WAL logs are replayed continuously to update its data
area.

Rotation happens on the secondary only. Since the data is kept up to date, we
don't need to go back to the primary any time. Only difference is that the
base data backup cannot use pg_start_backup() since the db is not yet up.

Backup Location
---------------
Backups are kept in /var/lib/postgresql/8.4/ha_backups (different from
hb_backups for HB). Rest all is same, including location of current backup.
WAL files are accumulated under the current backup.

WAL files are written directly from the primary onto the secondary's area.

Setup
-----
1. Ensure primary and secondary are running the same pg version, and that the
directory structure is the same.

2. Setup password-less login for root as well as postgres between the two.

3. Install postgresql-contrib on both servers. This provides a utility called
pg_standby, used for the standby server.

4. Settings /etc/hms/options:
   Primary:
     HA_OTHER_NODE=<secondary's IP>
     BACKUP_TYPE=partial
     HOT_BACKUP_ENABLED=N
	 CLOUDBKP_ENABLED=N
   Secondary:
     HA_OTHER_NODE=<primary's IP>
     HA_IS_STANDBY=Y
     BACKUP_TYPE=partial
     HOT_BACKUP_ENABLED=Y
	 CLOUDBKP_ENABLED=N

Of the above, CLOUDBKP_ENABLED=N should be setup after confirming that the
customer is following a regular manual disaster backup process.

5. Enable archive mode in postgresql.conf (postgres reload required), in both
primary and secondary:

  archive_mode = on
  archive_command = '/var/lib/postgresql/8.4/wal_archive.sh %p %f'
  archive_timeout = 300
  wal_level = archive		# only for postgresql 9.1 and above

This can be done on secondary any time, since it is not active. On primary,
we will need to ask for downtime if reload doesn't work and requires a restart.

6. Secondary Backup: on the secondary, set up a backup directory which uses
a secondary disk, just like in HB setup step 1. Since this is used only when
the primary fails, it can be setup as an external media at the time of
failure.

Initialization
--------------
Initialization is different from HB, we need an initial sync of the data from
primary to secondary to start off, and a copy in backup.

To do this, run ha_init.sh on the primary. This will also stop and restart 
postgres on the secondary and do some basic checks.

Check: login to the secondary and run psql. It should give an error saying
"FATAL: the database system is starting up".

Space usage estimate
--------------------
Estimates are same as hb for the backup portion on the secondary.

In addition to the backup portion, we also have the main directory synced
up from the primary.

Failover
--------
1. When the primary fails, run the command failover.sh on the secondary. Now,
the secondary will become active, also, the backup mode is now automatically
changed to Hot Backup.

2. If, in step 6 of Setup, the secondary disk was not set up, set it up now.

3. Initialize the hot backup by running hot_backup.sh once.

PITR Recovery
-------------
For PITR recovery, use the secondary and follow the same process as a HB
restoration with PITR. Only one change needs to be done, that is the
the location of WAL files has to be changed to ha_backups in recovery.conf.

