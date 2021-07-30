[Back to Development Guide](devguide_toc.md)

## Insta HMS Developer Environment Setup
__(On Ubuntu 14.04 or later)__

You'd need be root user to follow below instructions and also connected to ```practo``` SSID

### Installing Prerequisites

``` sh
export UBUNTUDIST=`lsb_release -c -s`
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.6/install.sh | bash
echo "export NVM_DIR=\"\$HOME/.nvm\"" >> ~/.bash_profile
echo "[ -s \"\$NVM_DIR/nvm.sh\" ] && . \"\$NVM_DIR/nvm.sh\"" >> ~/.bash_profile
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt/ $UBUNTUDIST-pgdg main" >> /etc/apt/sources.list.d/postgresql.list'
sudo add-apt-repository ppa:chris-lea/redis-server -y
sudo apt-get update
sudo apt-get install postgresql-contrib-10 postgresql-10 python3 python3-pip maven redis-server openjdk-8-jdk
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
sudo wget /usr/local/apache-tomcat-9.0.50.tar.gz https://mirrors.estointernet.in/apache/tomcat/tomcat-9/v9.0.50/bin/apache-tomcat-9.0.50.tar.gz
sudo tar -xvf /usr/local/apache-tomcat-9.0.50.tar.gz
sudo ln -s /usr/local/apache-tomcat-9.0.50 /usr/local/tomcat-9
sudo rm /usr/local/apache-tomcat-9.0.50.tar.gz
echo 'export PATH="/usr/local/tomcat-9/bin:$PATH"' >> ~/.bashrc
echo 'export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"' >> ~/.bashrc
source ~/.bashrc
nvm install v8.11.3
sudo createuser -s postgres
sudo sed --in-place -e 's/max_connections = 100/max_connections = 300/g' /etc/postgresql/10/main/postgresql.conf
sudo sed --in-place -e 's/md5/trust/g' /etc/postgresql/10/main/pg_hba.conf
sudo systemctl restart postgresql.service
```

### Clone repository and setup Environment Variables

``` sh
git clone git@github.com:practo/insta-hms.git
cd insta-hms
git checkout develop
git submodule init
git config submodule.insta-ui.url "git@github.com:practo/insta-ui.git"
git submodule update
export INSTAHMS_BASE=`pwd`/instahms
export TOMCAT_HOME=/usr/local/tomcat-9
export CATALINA_HOME=/usr/local/tomcat-9
export CATALINA_BASE=/usr/local/tomcat-9
sudo pip3 install -r $INSTAHMS_BASE/requirements.txt
cd $INSTAHMS_BASE/../insta-ui
./setup.sh
```

### Update maven settings to access Insta Private Maven Repository

Add your [github o-auth-token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) to maven's settings.xml file to access Insta Private Maven Repository. This is usually located at ```~/.m2/settings.xml```. If the file doesn't exist create one.

```xml
<settings xmlns="http://maven.apache.org/POM/4.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>insta-private-maven</id>
      <configuration>
        <httpHeaders>
          <property>
            <name>Authorization</name>
            <value>token {your-github-oauth-token}</value>
          </property>
        </httpHeaders>
      </configuration>
    </server>
  </servers>
</settings> 
```

### Database Setup

__Create Database__

``` sh
echo "CREATE DATABASE hms;"|psql -U postgres --no-psqlrc
echo "CREATE DATABASE hms_q;"|psql -U postgres --no-psqlrc
echo "CREATE SCHEMA quartz"|psql -U postgres -d hms_q
psql -U postgres -d hms_q -f $INSTAHMS_BASE/WEB-INF/install/sql/quartz_tables.sql
egrep -i "(CREATE extension|create schema)" $INSTAHMS_BASE/instahms/WEB-INF/install/bin/install_extensions.sh | psql -U postgres -d hms
```

__Create New Hospital Schema__

``` sh
cd $INSTAHMS_BASE
mvn clean package -Dinstaui.build.skip=true -DskipTests=true -Dcheckstyle.skip=true
cp $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties.config $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties
python3 create-initial-schema.py cenlocal hms $INSTAHMS_BASE/target/instahms
```

__Prepopulated Multicenter Schema for Development: cen__

To get a prepopulated multicenter database for development purposed you can run the following command

```sh
scp dev@instatestsvr2.practo.in:/home/dev/cen.tar.gz ~/.
tar -xvzf ~/cen.tar.gz
psql -Upostgres -d hms -f ~/cen.sql
echo "update cen.databasechangelog set filename=replace(filename,'/root/webapps/instahms1113','${INSTAHMS_BASE}/target/instahms'), md5sum = null;"|psql -U postgres -d hms
```

This would create hospital with the name 'cen'. Run liquibase migrations to update cen 

__Running Liquibase Migrations__

Liquibase is used for database migrations, Instructions to migrate the database to the latest version are:

Run maven build. Please also ensure that the folder ```/var/log/insta/``` has hms in it.

``` sh
   sudo mkdir -p /var/log/insta/hms
   cd $INSTAHMS_BASE
   mvn clean package -Dinstaui.build.skip=true -DskipTests=true
```

Create Extensions using the install_extensions.sh from bin folder

```sh
   ./$INSTAHMS_BASE/WEB-INF/install/bin/install_extensions.sh
```

In INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase; copy liquibase.update.properties.config to **liquibase.update.properties**

``` sh
  cp $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties.config $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties
```

Run upgrade_schema.py which is located <APPROOT>/target/instahms:

``` sh
sudo INSTA_ENV=dev python3 upgrade_schema.py <schema-name> <dbname> $INSTAHMS_BASE/target/instahms
```
Check logs in /var/log/insta/hms/liquibase.log. Fix any failures and re run liquibase.

**Repeat steps in this section to keep the database updated with latest liquibase changes; this will recreate all views and triggers(except auditlog triggers)**

Note: Auditlog triggers can be created from $INSTAHMS_BASE/WEB-INF/install/bin.

### Building & Deploying Insta HMS

__Symlink Common Libraries to Tomcat__

``` sh
sudo ln -s $INSTAHMS_BASE/../commonlib/*.jar $TOMCAT_HOME/lib
sudo mkdir -p $TOMCAT_HOME/endorsed
sudo ln -s $INSTAHMS_BASE/../commonlib/endorsed/* $TOMCAT_HOME/endorsed
```

__Build Insta HMS__

``` sh
cd $INSTAHMS_BASE
INSTAUI_LOCALES=en mvn clean package -DskipTests=true -Dcheckstyle.skip=true
```

__Build Insta API__ 

```
mvn -f pom.apps.xml clean package -e -Dcheckstyle.skip=true -DskipTests=true
```
__Setting up JNDI resource on Tomcat__

``` xml
        <Resource name="postgres" auth="Container"
            type="javax.sql.DataSource" factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
            url="jdbc:postgresql://localhost:5432/hms"
            username="postgres"
            driverClassName="org.postgresql.Driver"
            minIdle="10"
            maxWait="10000" maxIdle="50" maxActive="50"
            testOnBorrow="true" 
            validationQuery="select 1"
            testWhileIdle="true"
            minEvictableIdleTimeMillis="60000" timeBetweenEvictionRunsMillis="20000"
            validationInterval="30000"
            removeAbandoned="true" removeAbandonedTimeout="300" logAbandoned="true"
            jdbcInterceptors="StatementFinalizer;ResetAbandonedTimer"
        />
        <Resource name="quartz" auth="Container" type="javax.sql.DataSource"
            factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
            url="jdbc:postgresql://localhost:5432/hms_q"
            username="postgres"
            driverClassName="org.postgresql.Driver"
            minIdle="10"
            maxWait="10000" maxIdle="50" maxActive="50"
            testOnBorrow="true"
            validationQuery="select 1"
            testWhileIdle="true"
            minEvictableIdleTimeMillis="60000" timeBetweenEvictionRunsMillis="20000"
            validationInterval="30000"
            removeAbandoned="true" removeAbandonedTimeout="300" logAbandoned="true"
            jdbcInterceptors="StatementFinalizer;ResetAbandonedTimer" />
```

Copy above xml markup and add it inside ```<Context>``` of ```$TOMCAT_HOME/conf/context.xml```

__Setting up Insta API__

```
<Context path="/instaapps" debug="0" reloadable="false" crossContext="true"
        docBase="/root/webapps/instaapps">

        <Resource name="postgres" auth="Container"
        type="javax.sql.DataSource" factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
        url="jdbc:postgresql://127.0.0.1:5432/hms"
                username="postgres" password="postgres123"
                driverClassName="org.postgresql.Driver"
                minIdle="0"
                maxWait="10000" maxIdle="50" maxActive="50" REMOVE_FOR_TESTING="1"
                removeAbandoned="true" removeAbandonedTimeout="60" logAbandoned="true"
        jdbcInterceptors="StatementFinalizer;ResetAbandonedTimer"
        />
        <Parameter name="defaultSchema" value="__schema__" override="false"/>

        <Resource name="quartz" auth="Container" type="javax.sql.DataSource"
                factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
                url="jdbc:postgresql://127.0.0.1:5432/hms_q"
                username="postgres" password="postgres123"
                driverClassName="org.postgresql.Driver"
                minIdle="8"
                maxWait="10000" maxIdle="15" maxActive="15" REMOVE_FOR_TESTING="1"
                testOnBorrow="true"
                validationQuery="select 1"
                testWhileIdle="true"
                minEvictableIdleTimeMillis="60000" timeBetweenEvictionRunsMillis="20000"
                validationInterval="30000"
                removeAbandoned="true" removeAbandonedTimeout="300" logAbandoned="true"
                jdbcInterceptors="StatementFinalizer;ResetAbandonedTimer" />

</Context>
```
Copy the above markup into the file `$TOMCAT_HOME/conf/Catalina/localhost/instaapp.xml` 
(create one, if it does not exists). Also, replace `/root/webapps/instaapps` with path to the instaapps build Ex : `/home/ubuntu/insta-hms/instahms/target/instaapps`. Also,make sure the postgres  username and password is configured correctly as per your local.

__Deploy Insta API to Tomcat__
```
sudo ln -s $INSTAHMS_BASE/target/instaapps $CATALINA_BASE/webapps
```

__Deploy Insta HMS to Tomcat__

``` sh
sudo ln -s $INSTAHMS_BASE/target/instahms $CATALINA_BASE/webapps
catalina start
```

### Accessing local instance of Insta HMS

You can now browse your local instance on Insta HMS on http://localhost:8080/instahms

There are some default credentials that are available when schema gets created. Check ```cenlocal.u_user``` table in ```hms``` database.

Hospital name is ```cenlocal```

### Setup local development environment to use SSL

Follow below steps to install ssl certificate based proxy config in nginx.  

```sh
sudo apt-get install nginx-full
sudo mkdir -p /usr/local/etc/practodev
sudo mkdir /usr/share/ca-certificates/extra
cd $INSTAHMS_BASE/../local-ca-certs
sudo cp instahms.local.ca.crt /usr/share/ca-certificates/extra/instahms.local.ca.crt
sudo cp instahms.local.crt instahms.local.key /usr/local/etc/practodev
sudo cp instahms.local /etc/nginx/sites-enabled/instahms.local
sudo unlink /etc/nginx/sites-enabled/default
sudo update-ca-certificates
sudo service nginx restart
echo '127.0.0.1 www.instahms.local' | sudo tee -a /etc/hosts > /dev/null
```

Once done Insta HMS and API can be accessed using https://www.instahms.local instead of http://localhost:8080 . The config assumes the tomcat to be serving requests at port 8080. If your local development environment uses a different port then change the same in file `/etc/nginx/sites-enabled/instahms.local` and restart nginx using command `sudo service nginx restart`

### Test Servers

We have test servers on LAN and on AWS, which you can access to reproduce bugs based on varying configurations. Add following lines to /etc/hosts to access these servers. You need to be connected to "practo" SSID to have access to these servers. For remotely accessing these servers connect to office VPN on ```123.63.119.25``` using [forticlient](https://www.forticlient.com/downloads) and credentials provided for "practo" SSID.


You can access testserver instances for current RC version by accessing http://<server>/instahms or older version by accessing http://<server>/instahms<version> where version is version without dot (ex: 11.7 => 117, 11.10=>1110)
