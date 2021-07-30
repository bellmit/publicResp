sudo touch /var/log/insta/ip_visit_migration.log;

[ -z $1 ] || [ -z $2 ] && echo "Usage: $0 <schema> <url upto context path, ex: http://localhost:8080/instahms>" && exit 1

#hospital in which we want to migrate
schema=$1;
#url upto context path
url=$2

echo 'logging in...';
l_response_code=`curl -c cookies.txt -sL -w "%{http_code}\\n" -d "userid=InstaAdmin&password=InstaAdmin&hospital=$schema" "$url/login.do?" -o /var/log/insta/ip_visit_migration.log`;

#looking for quick links, this is because if the login is successful it forwards the request to the home page.
#if it finds the quick links means that the login is successful. otherwise failed to login in. see error details in the log file.
home_str=`sudo grep -l "Quick Links" /var/log/insta/ip_visit_migration.log`;

if [ -s /var/log/insta/ip_visit_migration.log -a  -z "${home_str}" ] ; then
    echo "Response Code : $l_response_code, Failed to login : pls check the log file for detailed description";
elif [ $l_response_code -eq 200 ] ; then
    echo 'Login Successful.. Sending request to migrate the data';
    u_response_code=`curl -b cookies.txt -sL -w "%{http_code}\\n" "$url/migration/IpConsultationMigration.do?_method=migrate" -o /var/log/insta/ip_visit_migration.log`
    success_str=`sudo grep -l "Migration Successful.." /var/log/insta/ip_visit_migration.log`;

    if [ -s /var/log/insta/ip_visit_migration.log -a -z "${success_str}" ] ; then
        echo "Response Code : $u_response_code, Failed to migrate the data: pls check the log file for detailed description";
    elif [ $u_response_code -eq 200 ] ; then
        echo "Successfully migrated ip consultation data as a generic document for the patient";
    else
        echo "Response Code : $u_response_code Failed to migrate the data: pls check the log file for detailed description";
    fi
else
    echo "Response Code : $u_response_code Failed to login : pls check the log file for detailed description";
fi

