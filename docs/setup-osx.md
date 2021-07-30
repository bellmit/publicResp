[Back to Development Guide](devguide_toc.md)

## Insta HMS Developer Environment Setup

**(On OSX)**

### Installing Prerequisites

```sh
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.35.3/install.sh | bash
echo "export NVM_DIR=\"\$HOME/.nvm\"" >> ~/.bash_profile
echo "[ -s \"\$NVM_DIR/nvm.sh\" ] && . \"\$NVM_DIR/nvm.sh\"" >> ~/.bash_profile
brew tap petere/postgresql
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk8
brew install tomcat@9
brew install postgresql@10
brew link -f postgresql@10
brew install postgresql-common
brew install maven
brew install redis
brew tap bukalapak/packages
brew install snowboard
echo 'export PATH="/usr/local/opt/postgresql@10/bin:$PATH"' >> ~/.bash_profile
echo 'export PATH="/usr/local/opt/tomcat@9/bin:$PATH"' >> ~/.bash_profile
echo 'export JAVA_HOME="$(/usr/libexec/java_home -v 1.8 -f)"' >> ~/.bash_profile
echo 'export PGDATA="/usr/local/var/postgresql10"' >> ~/.bash_profile
source ~/.bash_profile
nvm install v8.14.0
initdb -D $PGDATA
echo 'max_connections=300' >> $PGDATA/postgresql.conf
echo 'shared_buffers=80MB' >> $PGDATA/postgresql.conf
pg_ctl start&
createuser -s postgres
```

### Clone repository and setup Environment Variables

```sh
git clone git@github.com:practo/insta-hms.git
cd insta-hms
git checkout develop
git submodule init
git config submodule.insta-ui.url "git@github.com:practo/insta-ui.git"
git submodule update
echo 'export INSTAHMS_BASE=`pwd`/instahms' >> ~/.bash_profile
echo 'export TOMCAT_HOME=/usr/local/opt/tomcat@9/libexec' >> ~/.bash_profile
echo 'export CATALINA_HOME=/usr/local/opt/tomcat@9/libexec' >> ~/.bash_profile
echo 'export CATALINA_BASE=/usr/local/opt/tomcat@9/libexec' >> ~/.bash_profile
pip install -r $INSTAHMS_BASE/requirements.txt
cd $INSTAHMS_BASE/../insta-ui
./setup.sh
```

### Update maven settings to access Insta Private Maven Repository

Add your [github o-auth-token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) to maven's settings.xml file to access Insta Private Maven Repository. This is usually located at `~/.m2/settings.xml`. If the file doesn't exist create one. Replace {your-github-oauth-token} with your oauth token.

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

**Create Database**

```sh
echo "CREATE DATABASE hms;"|psql -U postgres --no-psqlrc
echo "create language plperl;"|psql -U postgres -d hms
echo "CREATE DATABASE hms_q;"|psql -U postgres --no-psqlrc
echo "create language plperl;"|psql -U postgres -d hms_q
echo "CREATE SCHEMA quartz"|psql -U postgres -d hms_q
psql -U postgres -d hms_q -f $INSTAHMS_BASE/WEB-INF/install/sql/quartz_tables.sql
egrep -i "(CREATE extension|create schema)" $INSTAHMS_BASE/instahms/WEB-INF/install/bin/install_extensions.sh | psql -U postgres -d hms
```

**Create New Hospital Schema**

```sh
cd $INSTAHMS_BASE
mvn clean package -Dinstaui.build.skip=true -DskipTests=true -Dcheckstyle.skip=true -Dhttps.protocols=TLSv1.2
cp $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties.config $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties
./$INSTAHMS_BASE/WEB-INF/install/bin/install_extensions.sh
python3 create-initial-schema.py cenlocal hms $INSTAHMS_BASE/target/instahms
```

**Prepopulated Multicenter Schema for Development: cen**

To get a prepopulated multicenter database for development purposed you can run the following command

```sh
scp dev@instatestsvr2.practo.in:/home/dev/cen.tar.gz ~/.
tar -xvzf ~/cen.tar.gz
psql -Upostgres -d hms -f ~/cen.sql
echo "update cen.databasechangelog set filename=replace(filename,'/root/webapps/instahms1113','${INSTAHMS_BASE}/target/instahms'), md5sum = null;"|psql -U postgres -d hms
```

This would create hospital with the name 'cen'. Run liquibase migrations to update cen

**Running Liquibase Migrations**

Liquibase is used for database migrations, Instructions to migrate the database to the latest version are:

Run maven build. Please also ensure that the folder `/var/log/insta/` has hms in it.

```sh
   sudo mkdir -p /var/log/insta/hms
   cd $INSTAHMS_BASE
   mvn clean package -Dinstaui.build.skip=true -DskipTests=true
```

Create Extensions using the install_extensions.sh from bin folder

```sh
   ./$INSTAHMS_BASE/WEB-INF/install/bin/install_extensions.sh
```

In INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase; copy liquibase.update.properties.config to **liquibase.update.properties**

```sh
  cp $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties.config $INSTAHMS_BASE/target/instahms/WEB-INF/classes/liquibase/liquibase.update.properties
```

Run upgrade_schema.py which is located <APPROOT>/target/instahms:

```sh
sudo INSTA_ENV=dev python3 upgrade_schema.py <schema-name> <dbname> $INSTAHMS_BASE/target/instahms
```

Check logs in /var/log/insta/hms/liquibase.log. Fix any failures and re run liquibase.

**Repeat steps in this section to keep the database updated with latest liquibase changes; this will recreate all views and triggers(except auditlog triggers)**

Note: Auditlog triggers can be created from $INSTAHMS_BASE/WEB-INF/install/bin.

### Building & Deploying Insta HMS

**Symlink Common Libraries to Tomcat**

```sh
sudo cp $INSTAHMS_BASE/../commonlib/*.jar $TOMCAT_HOME/lib
sudo rm $TOMCAT_HOME/lib/postgresql-9.3-1101.jdbc4.jar
sudo mkdir -p $TOMCAT_HOME/endorsed
sudo ln -s $INSTAHMS_BASE/../commonlib/endorsed/* $TOMCAT_HOME/endorsed

```

**Configuring Tomcat 9**

```sh
#add or edit setenv.sh
vi $TOMCAT_HOME/bin/setenv.sh
```

Add following properties in `setenv.sh`

```sh
#default Java 8 for tomcat@9 on boot up
JAVA_HOME="$(/usr/libexec/java_home -v 1.8 -f)"
#allot heap space.(Developer can tweak heap memory values as per usecase)
JAVA_OPTS="$JAVA_OPTS -Djava.awt.headless=true -XX:+CMSClassUnloadingEnabled -Xms1024m -Xmx2G -XX:MaxMetaspaceSize=512m"
```

Note: If Tomcat 7 is already installed on the machine, and to have multiple instances of tomcat, change ports used by **tomcat@9**. Example:
**tomcat@7** uses 8080, **tomcat@9** can be configured to 9090 and also other configurations to use 9 series.
File to be edited: `$TOMCAT_HOME/conf/server.xml`

**Build Insta HMS**

```sh
cd $INSTAHMS_BASE
INSTAUI_LOCALES=en mvn clean package -DskipTests=true
```

**Setting up JNDI resource on Tomcat**

```xml
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
            jdbcInterceptors="StatementFinalizer;ResetAbandonedTimer"
        />
```

Copy above xml markup and add it inside `<Context>` of `$TOMCAT_HOME/conf/context.xml`

For **tomcat@9** setup add following property under `<Context>` to stop Jar scanning which is true by default in **tomcat@9**

```xml
<JarScanner scanManifest="false" scanClassPath="false"/>
```

**Start redis & Deploy Insta HMS to Tomcat**

```sh
sudo ln -s $INSTAHMS_BASE/target/instahms $CATALINA_BASE/webapps
brew services start redis
catalina start
```

### Accessing local instance of Insta HMS

You can now browse your local instance on Insta HMS on http://localhost:8080/instahms

There are some default credentials that are available when schema gets created. Check `cenlocal.u_user` table in `hms` database.

Hospital name is `cenlocal`

**Build Insta API**

```sh
mvn -f pom.apps.xml clean package -DskipTests=true
```

**Setting up Insta API**

```xml
<Context path="/instaapps" debug="0" reloadable="false" crossContext="true"
        docBase="/root/webapps/instaapps">

        <Resource name="postgres" auth="Container"
        type="javax.sql.DataSource" factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
        url="jdbc:postgresql://127.0.0.1:5432/hms"
                username="postgres"
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
                username="postgres"
                driverClassName="org.postgresql.Driver"
                minIdle="8"
                maxWait="10000" maxIdle="15" maxActive="15" REMOVE_FOR_TESTING="1"
                testOnBorrow="true"
                validationQuery="select 1"
                testWhileIdle="true"
                minEvictableIdleTimeMillis="60000" timeBetweenEvictionRunsMillis="20000"
                validationInterval="30000"
                removeAbandoned="true" removeAbandonedTimeout="300" logAbandoned="true"
                jdbcInterceptors="StatementFinalizer;ResetAbandonedTimer"
        />

</Context>
```

Copy the above markup into the file `$TOMCAT_HOME/conf/Catalina/localhost/instaapp.xml`
(create one, if it does not exists). Also, replace `/root/webapps/instaapps` with path to the instaapps build Ex : `/home/insta-hms/instahms/target/instaapps` . Also,make sure the postgres username and password is configured correctly as per your local.

**Deploy Insta API to Tomcat**

```sh
sudo ln -s $INSTAHMS_BASE/target/instaapps $CATALINA_BASE/webapps
```

### Setup local development environment to use SSL

Follow below steps to install ssl certificate based proxy config in nginx.  

```sh
brew install nginx
mkdir -p /usr/local/etc/practodev
cd $INSTAHMS_BASE/../local-ca-certs
sudo security add-trusted-cert -d -r trustRoot -k "/Library/Keychains/System.keychain" instahms.local.ca.pem
cp instahms.local.crt instahms.local.key /usr/local/etc/practodev
cp instahms.local.osx /usr/local/etc/nginx/servers/instahms.local.conf
brew services restart nginx
echo '127.0.0.1 www.instahms.local' >> /etc/hosts
```
Once done Insta HMS and API can be accessed using https://www.instahms.local instead of http://localhost:8080 . The config assumes the tomcat to be serving requests at port 8080. If your local development environment uses a different port then change the same in file `/usr/local/etc/nginx/servers/instahms.local.conf` and restart nginx using command `brew services restart nginx`


### Test Servers

We have test servers on LAN and on AWS, which you can access to reproduce bugs based on varying configurations. Add following lines to /etc/hosts to access these servers. You need to be connected to "practo" SSID to have access to these servers. For remotely accessing these servers connect to office VPN on `123.63.119.25` using [forticlient](https://www.forticlient.com/downloads) and credentials provided for "practo" SSID.

You can access testserver instances for current RC version by accessing http://<server>/instahms or older version by accessing http://<server>/instahms<version> where version is version (major version \* 100) + (minor version) (ex: 12.1 => 1201, 11.10=>1110)
