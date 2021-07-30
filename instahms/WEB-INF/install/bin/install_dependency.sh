#!/bin/bash

source `dirname $0`/functions

valid_install_message='Status: install ok installed'

aptpack_to_add=''

redis_status="$(sudo dpkg -s redis-server|grep Status)"
if [ "$redis_status" != "$valid_install_message" ] ; then
   sudo add-apt-repository ppa:chris-lea/redis-server -y
   aptpack_to_add="redis-server $aptpack_to_add"
fi

ISPRECISE="$(lsb_release -s -c | grep precise)"
if [ ! -z "$PGHOST" ] ; then
	lib_pq_status="$(ssh root@$PGHOST \"sudo dpkg -s libpq-dev|grep Status\")"
else
	lib_pq_status="$(sudo dpkg -s libpq-dev|grep Status)"
fi

if [ "$lib_pq_status" != "$valid_install_message" ] ; then
	if [ "$ISPRECISE" != "" ] ; then
		sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ precise-pgdg main" >> /etc/apt/sources.list.d/postgresql.list'
   		aptpack_to_add="postgresql-server-dev-9.3 $aptpack_to_add"
	else
   		aptpack_to_add="libpq-dev $aptpack_to_add"
	fi
	
fi

build_essential_status="$(sudo dpkg -s build-essential|grep Status)"
if [ "$build_essential_status" != "$valid_install_message" ] ; then
	aptpack_to_add="build-essential $aptpack_to_add"
fi

python3_status="$(sudo dpkg -s python3|grep Status)"
if [ "$python3_status" != "$valid_install_message" ] ; then
	aptpack_to_add="python3 $aptpack_to_add"
fi

python3_dev_status="$(sudo dpkg -s python3-dev|grep Status)"
if [ "$python3_dev_status" != "$valid_install_message" ] ; then
	aptpack_to_add="python3-dev $aptpack_to_add"
fi

libpython3_dev_status="$(sudo dpkg -s libpython3-dev|grep Status)"
if [ "$libpython3_dev_status" != "$valid_install_message" ] ; then
	aptpack_to_add="libpython3-dev $aptpack_to_add"
fi

python3_pip_status="$(sudo dpkg -s python3-pip|grep Status)"
if [ "$python3_pip_status" != "$valid_install_message" ] ; then
	aptpack_to_add="python3-pip $aptpack_to_add"
fi

libssl_dev_status="$(sudo dpkg -s libssl-dev|grep Status)"
if [ "$libssl_dev_status" != "$valid_install_message" ] ; then
	aptpack_to_add="libssl-dev $aptpack_to_add"
fi

libffi_dev_status="$(sudo dpkg -s libffi-dev|grep Status)"
if [ "$libffi_dev_status" != "$valid_install_message" ] ; then
	aptpack_to_add="libffi-dev $aptpack_to_add"
fi

libffi_dev_status="$(sudo dpkg -s w3m|grep Status)"
if [ "$libffi_dev_status" != "$valid_install_message" ] ; then
	sudo apt-get update
	sudo apt-get install -y w3m
fi

postgres_contrib="postgresql-contrib-9.3"
if [[ $PG_VER_NUM -gt 1000 ]] ; then
	postgres_contrib="postgresql-contrib-10"
fi

if [ ! -z "$PGHOST" ] ; then
	postgres_contrib_status="$(ssh root@$PGHOST \"sudo dpkg -s postgres_contrib|grep Status\")"
else
	postgres_contrib_status="$(sudo dpkg -s $postgres_contrib|grep Status)"
fi

if [ "$postgres_contrib_status" != "$valid_install_message" ] ; then
	if [ ! -z "$PGHOST" ] ; then
		ssh root@$PGHOST "sudo apt-get update"
		ssh root@$PGHOST "sudo apt-get install -y $postgres_contrib"
	else
		aptpack_to_add="$postgres_contrib $aptpack_to_add"
	fi
fi

jq_status="$(sudo dpkg -s jq|grep Status)"
if [ "$jq_status" != "$valid_install_message" ] ; then
	aptpack_to_add="jq $aptpack_to_add"
fi

if [ ! -z "$aptpack_to_add" ] ; then
	sudo apt-get update
	echo "Installing apt packages: $aptpack_to_add"
	sudo apt-get install -y $aptpack_to_add
fi
#minio executable should be in /usr/local/bin/minio
if [ ! -e "/usr/local/bin/minio" ] ; then
	sudo wget https://dl.minio.io/server/minio/release/linux-amd64/archive/minio.RELEASE.2019-05-02T19-07-09Z -O /usr/local/bin/minio
	sudo curl -O https://raw.githubusercontent.com/minio/minio-service/cc4012fff4dbd57e2f495bcbac8b555090f9a724/linux-systemd/minio.service
	sudo chmod +x /usr/local/bin/minio
	sudo mv minio.service /etc/systemd/system
	sudo systemctl daemon-reload
fi

