#!/bin/bash

#
# Checks if any SSL cert is expiring in the near future, and send mail
# about the certificates that are going to expire
#

KEY_DIR="$HMS_WORK_HOME/files"
RECIPIENTS="deepak.ak@practo.com anupama.mr@practo.com alok.n@practo.com vijay.mudhol@practo.com"
HOST=`hostname`
HOST=${HOST#*-}


todaySec=`date +%s`
days=30
nearFutureSec=$((todaySec+days*86400))

rm -f /tmp/sslcertexpiry.txt

for crt in $KEY_DIR/*.crt ; do
	enddate=`/usr/bin/openssl x509 -enddate -in $crt -noout`
	enddateSec=`date +%s --date="${enddate#*=}"`
	if [ $enddateSec -lt $nearFutureSec ] ; then
		echo "  ${crt##*/} is expiring soon (${enddate#*=})" >> /tmp/sslcertexpiry.txt
	fi
done

[ ! -f /tmp/sslcertexpiry.txt ] && exit 0

headers=`cat <<EOF
From: SSL Cert Expiry Check <root@$HOST>
To: $RECIPIENTS
Subject: SSL Certificates about to expire

The following SSL Certificates on $HOST  are about to
expire in the next $days days. Please read the instructions under $HMS_WORK_HOME/README.txt
to renew and re-install the certificates. If the certificates are not to be used
any longer, delete them from under $HMS_WORK_HOME/files/ directory.


EOF
`

echo "$headers" | cat - /tmp/sslcertexpiry.txt | \
  /usr/sbin/sendmail -r root@apps.instahealthsolutions.com $RECIPIENTS

