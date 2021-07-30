$2/pack-hl7.sh $2
dt=`date +"%F %T"`
if [ "$?" = "0" ]; then
    echo "$dt: HL7 result files packed."
    sudo rsync -azP $2/order-packet.tar.gz root@hlmbilling:$1/
    if [ "$?" = "0" ]; then
        echo "$dt: rsync done."
        sudo rm $2/order-packet.tar.gz
        ssh root@hlmbilling "bash -s" < $2/extract-and-cleanup.sh "$1"
        if [ "$?" = "0" ]; then
            echo "$dt: Extracted results on HIS. Clean up done."
            exit 0
        fi
    fi
fi
exit 1
