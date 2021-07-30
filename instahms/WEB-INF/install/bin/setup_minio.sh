source `dirname $0`/functions
LOGFILE=$LOGDIR/miniosetup.log
mkdir -p /var/log/journal
if [ -z "$MINIOUSER" ]; then
	do_log "Using default value for minio user"
	MINIOUSER="miniouser"
fi

if [ -z "$MINIODIRECTORY"]; then
	do_log "Using default value for minio data directory"
	MINIODIRECTORY="/home/$MINIOUSER/data"
fi

if [ -z "$MINIOHOSTIP"]; then
	do_log "Using localhost for minio hosted ip"
	MINIOHOSTIP="localhost"
fi

do_log "Generating access key and secret key"
MINIO_ACCESS_KEY="$RANDOM"
MINIO_SECRET_KEY="`uuidgen`"
sudo sed --in-place -e "s/<minio-access-key>/$MINIO_ACCESS_KEY/g" $APPROOT/WEB-INF/classes/java/resources/environment.properties
sudo sed --in-place -e "s/<minio-secret-key>/$MINIO_SECRET_KEY/g" $APPROOT/WEB-INF/classes/java/resources/environment.properties

do_log "Creating minio user; data directory and config directory"
sudo useradd -r $MINIOUSER -s /sbin/nologin
sudo chown $MINIOUSER:$MINIOUSER /usr/local/bin/minio
sudo mkdir -p $MINIODIRECTORY
sudo chown $MINIOUSER:$MINIOUSER $MINIODIRECTORY
sudo mkdir -p /etc/minio
sudo chown $MINIOUSER:$MINIOUSER /etc/minio
sudo cp $APPROOT/WEB-INF/install/bin/minio.conf /etc/default/minio
sudo sed --in-place -e "s#<minio-directory>#$MINIODIRECTORY#g" /etc/default/minio
sudo sed --in-place -e "s/<minio-host-ip>/$MINIOHOSTIP/g" /etc/default/minio
sudo sed --in-place -e "s/<minio-access-key>/$MINIO_ACCESS_KEY/g" /etc/default/minio
sudo sed --in-place -e "s/<minio-secret-key>/$MINIO_SECRET_KEY/g" /etc/default/minio

do_log "Configuring minio service"
sudo sed --in-place -e "s/User=minio-user/User=$MINIOUSER/g" /etc/systemd/system/minio.service
sudo sed --in-place -e "s/Group=minio-user/Group=$MINIOUSER/g" /etc/systemd/system/minio.service
sudo systemctl daemon-reload

##ssl setup for minio
do_log "performing ssl setup for minio"
sudo mkdir -p /etc/minio/certs
sudo openssl ecparam -genkey -name prime256v1 | sudo openssl ec -out /etc/minio/certs/private.key
sudo openssl req -new -x509 -days 3650 -key /etc/minio/certs/private.key -out /etc/minio/certs/public.crt -subj "/C=US/ST=state/L=location/O=organization/CN=minio.localhost.com"
sudo chown $MINIOUSER:$MINIOUSER /etc/minio/certs/private.key
sudo chown $MINIOUSER:$MINIOUSER /etc/minio/certs/public.crt
do_log "Certs generated and moved successfully; starting minio"
sudo systemctl start minio
sudo systemctl is-active minio.service
if [ $? -ne 0 ] ; then
	do_log "Unable to start minio; exiting"
	exit 1
fi
