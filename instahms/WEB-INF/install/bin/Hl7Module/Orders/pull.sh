ssh root@hlmbilling "bash -s" < $2/pack-hl7.sh "$1"
dt=`date +"%F %T"`
if [ "$?" = "0" ]; then
    echo "$dt: hl7 files packed"
    $2/sync-and-delete.sh $1 $2
    if [ "$?" = "0" ]; then
        echo "$dt: sync and delete done."
        tar -C $2 -zxmf $2/order-packet.tar.gz
        find $2/order-packet/ -iname '*.hl7' -exec mv '{}' $2/in \;
        rm -r $2/order-packet
        rm $2/order-packet.tar.gz
        exit 0
    fi
fi
exit 1
