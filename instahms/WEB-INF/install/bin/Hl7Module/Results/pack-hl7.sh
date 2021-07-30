mkdir $1/order-packet
dt=`date +"%F %T"`
find $1/in/ -iname '*.hl7' -mmin +1 -exec mv '{}' $1/order-packet/ \;
if [ "$(ls -A $1/order-packet)" ]; then
    tar -C $1 -zcf $1/order-packet.tar.gz order-packet/
else
    echo "$dt: No new results generated."
    rm -r $1/order-packet
    exit 1
fi
rm -r $1/order-packet
exit 0
