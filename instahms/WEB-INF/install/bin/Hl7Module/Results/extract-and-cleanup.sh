tar -zxmf $1/order-packet.tar.gz -C $1
find $1/order-packet/ -iname '*.hl7' -exec mv '{}' $1/in/ \;
rm -r $1/order-packet
rm $1/order-packet.tar.gz
exit 0
