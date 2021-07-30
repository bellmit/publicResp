#!/bin/bash

#
# Syncrhonize users between apps and all servers
# Application users: hms81:gccdemo is the master
# Linux users: apps is the master
#

source `dirname $0`/functions
rsync="sudo /usr/bin/rsync"

# Disable if not a local server (eg testsvr)
is_local_server || exit 0

appver=`get_current_version_major` || 0
filename=pgusers
if [ $appver -ge 901 ] ; then
	filename=pgusers_encrypted
fi

if [ $HOST == 'demo' ] ; then
	[ -z $USERS_DB ] && USERS_DB=hms81
	[ -z $USERS_SCHEMA ] && USERS_SCHEMA=gccdemo
	# Dump the user list from the database, this is the master user list.
	# psql does not have rights to /root/ so we need to write to tmp and copy it over as root
	if [ $PGDATABASE == $USERS_DB ] ; then
		if [ $appver -ge 901 ] ; then
			psql -d $USERS_DB -c "\copy (SELECT emp_username, emp_password, emp_status, is_encrypted FROM $USERS_SCHEMA.u_user
				WHERE role_id=1 AND emp_username LIKE E'Insta\_%') to ""'""/tmp/$filename.dump""'"
		else
			psql -d $USERS_DB -c "\copy (SELECT emp_username, emp_password, emp_status FROM $USERS_SCHEMA.u_user
				WHERE role_id=1 AND emp_username LIKE E'Insta\_%') to ""'""/tmp/$filename.dump""'"
		fi
		if [ $? -eq 0 ] ; then
			# sync it to HUB
			$rsync -az /tmp/$filename.dump $HUBVPN::apps_users/$filename.dump
			# for 7.5: it uses an older area
			$rsync -az /tmp/$filename.dump $HUBVPN::old_apps_users/$filename.dump
		fi
	fi
	exit 0
fi

# On local servers: Sleep a little to ensure the same job on apps has run
sleep 15

sudo mkdir -p /root/users
$rsync -az $HUBVPN::builds/users/$filename.dump /root/users/
[ $? -ne 0 ] && echo "rsync failed, could not get latest users" && exit 1
# check for changes, and only then do something with it
diff -q /root/users/$filename.dump /root/users/$filename.dump.current > /dev/null
[ $? -eq 0 ] && exit
# import the dump and use it to update the current users
sudo cp /root/users/$filename.dump /tmp/
if [ $appver -ge 901 ] ; then
script='\set ON_ERROR_STOP
	DROP TABLE IF EXISTS tmp_users;
	CREATE TABLE tmp_users (name text, pwd text, status text, isencrypted boolean);
	\copy tmp_users FROM '"'""/tmp/$filename.dump""'"';
	-- Insert any new users
	INSERT INTO u_user (emp_username, emp_password, emp_status, is_encrypted, role_id)
		(SELECT name, pwd, status, isencrypted, 1 FROM tmp_users t
		WHERE NOT EXISTS (SELECT * FROM u_user WHERE emp_username = t.name));
	-- Delete any removed users
	DELETE FROM u_user WHERE role_id=1 AND emp_username LIKE E'"'"'Insta\_%'"'"' AND
		NOT EXISTS (SELECT * FROM tmp_users WHERE name=emp_username);
	-- Update status and password for existing users
	UPDATE u_user u SET emp_password=t.pwd, emp_status=t.status, is_encrypted=t.isencrypted
		FROM tmp_users t WHERE t.name = u.emp_username;
	UPDATE u_user u SET hosp_user = '"'"'N'"'"' WHERE u.role_id = 1 AND emp_username LIKE E'"'"'Insta\_%'"'"';
'
else
script='\set ON_ERROR_STOP
	DROP TABLE IF EXISTS tmp_users;
	CREATE TABLE tmp_users (name text, pwd text, status text);
	\copy tmp_users FROM '"'""/tmp/$filename.dump""'"';
	-- Insert any new users
	INSERT INTO u_user (emp_username, emp_password, emp_status, role_id)
		(SELECT name, pwd, status, 1 FROM tmp_users t
		WHERE NOT EXISTS (SELECT * FROM u_user WHERE emp_username = t.name));
	-- Delete any removed users
	DELETE FROM u_user WHERE role_id=1 AND emp_username LIKE E'"'"'Insta\_%'"'"' AND
		NOT EXISTS (SELECT * FROM tmp_users WHERE name=emp_username);
	-- Update status and password for existing users
	UPDATE u_user u SET emp_password=t.pwd, emp_status=t.status
		FROM tmp_users t WHERE t.name = u.emp_username;
	UPDATE u_user u SET hosp_user = '"'"'N'"'"' WHERE u.role_id = 1 AND emp_username LIKE E'"'"'Insta\_%'"'"';
'
fi

run_stmt_in_all_schemas "$script" "Updating users: "
sudo cp /root/users/$filename.dump /root/users/$filename.dump.current
