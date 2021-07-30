sudo rsync -azP root@hlmbilling:$1/order-packet.tar.gz $2 # tar may not exist
dt=`date +"%F %T"`
if [ "$?" = "0" ]; then
    echo "$dt: rsync done"
    ssh root@hlmbilling rm $1/order-packet.tar.gz # connection loss
    if [ "$?" = "0" ]; then
        exit 0 # connection loss
    fi
fi
exit 1
