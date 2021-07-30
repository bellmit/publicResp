mkdir $1/order-packet # order-packet might already exist
find $1/in/ -iname '*.hl7' -mmin +1 -exec mv '{}' $1/order-packet/ \; # this might get bloated. connection lost.
dt=`date +"%F %T"`
if [ "$(ls -A $1/order-packet)" ]; then
    tar -C $1 -zcf $1/order-packet.tar.gz order-packet/ # connection lost
else
    echo "$dt: Folder empty in $1/order-packet. No new HL7 files."
    rm -r $1/order-packet # connection lost
    exit 1 # connection lost, we might not know exit status
fi
rm -r $1/order-packet # connection lost
exit 0
