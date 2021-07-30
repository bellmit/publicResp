#!/bin/bash

[ -f /etc/hms/options ] && source /etc/hms/options

if [ -z "$TOMCAT_HOME" ] ; then
  TOMCAT_HOME="/usr/local/tomcat-9"
fi

get_full_version() {
    VERSION=`$TOMCAT_HOME/bin/catalina.sh version`
    VERSION=${VERSION##*Server number:}
    VERSION=${VERSION%%OS Name*}
    echo $VERSION
}

get_major_version(){
    arg1=`get_full_version`
    echo "${arg1%%.*}"
}

is_correct_version_deployed(){
  if [ `get_major_version` -lt 9 ]
  then
      echo "Please install the correct version of tomcat"
      exit 1;
   fi
}

is_tomcat9(){
    echo " Current tomcat version = `get_major_version`"
  if [ `get_major_version` -lt 9 ]
  then

      ls /usr/local/apache-tomcat-9.0.38 >>/dev/null                                  
                                                                                      
      if [ $? -eq "0" ] 
      then
           sudo ln -s /usr/local/apache-tomcat-9.0.38 tomcat-9
           echo "Linked tomcat-9"                                                      
           echo "Proceeding upgrade.."                                                 
      else                           
          echo "Please install the correct version of tomcat 9"
          exit 1;
      fi
   fi
}