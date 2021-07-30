#!/bin/bash

#
# Encrypts the key and set in apiconfig.properites 
# @commandlines argument (parameterName key)
#

source `dirname $0`/functions
JAVA=/usr/bin/java
export CLASSPATH=$APPROOT/WEB-INF/classes

key=`java com.insta.hms.common.AesEncryption $2`
sudo sed --in-place -e "s/$1\s*=.*$/$1 = $key/" ../../classes/apiconfig.properties
