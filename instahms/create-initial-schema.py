import subprocess
import psycopg2
import os
import re
import argparse
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import logging

OPTIONS_FILE = "/etc/hms/options"

parser = argparse.ArgumentParser(description='Liquibase update for a given schema and database')
parser.add_argument('schema', type=str, help='Postgresql schema name')
parser.add_argument('dbname', type=str, help='Postgresql db name')
parser.add_argument('approot', type=str, help='Root of the HMS application')
parser.add_argument('precision', type=int, nargs='?', help='precision_3 or precision_2', default = '2', choices=set((2,3)))

args = parser.parse_args()
schema = args.schema
dbname = args.dbname
approot = args.approot
precision = args.precision

logging_directory = os.path.join(os.environ.get('INSTA_LOGPATH','/var/log/insta'), dbname)
if not os.path.exists(logging_directory):
    os.makedirs(logging_directory)

logging.basicConfig(format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s', filename=logging_directory + '/liquibase.log',
                    level=logging.DEBUG, datefmt='%d/%m/%Y %I:%M:%S %p')
logging.info('Creating new schema:' + schema)

environment_type = os.environ.get('INSTA_ENV','prod')

liquibase_folder_path = approot + '/WEB-INF/classes/liquibase/'
migration_folder_path = approot + '/WEB-INF/classes/migrations/'
liquibase_jar_path = approot + '/WEB-INF/lib/liquibase-core-3.5.0.jar'

#read db host and db password from /etc/hms/options
if dbname != "hms":
        OPTIONS_FILE = OPTIONS_FILE + "." + dbname
host=""
dbpassword=""
dbuser=""
dbport=""
if (os.path.isfile(OPTIONS_FILE)):
    options_file_content = open(OPTIONS_FILE).readlines()
    for line in options_file_content:
        if 'DBHOST' in line and "#" not in line:
            host = line.split("=")[1].strip()
        if 'DBPASSWORD' in line and "#" not in line:
            dbpassword = line.split("=")[1].strip()
        if 'DBUSER' in line and "#" not in line:
            dbuser = line.split("=")[1].strip()
        if 'DBPORT' in line and "#" not in line:
            dbport = line.split("=")[1].strip()

if host == "":
    host = 'localhost'
if dbuser == "":
    dbuser = 'postgres'
if dbport == "":
    dbport = '5432'
logging.info("Host detected as:" + host + " Password detected as:" + dbpassword)

if dbport == "":
    dbport = '5432'
logging.info(" Port detected as:" + dbport)

#replace schema and db name in the file
if environment_type == 'prod': 
    with open(liquibase_folder_path + "liquibase.update.properties", 'r') as file:
        filedata = file.read()
    filedata = filedata.replace('__host__', host)
    filedata = filedata.replace('__APPROOT__', approot)
    filedata = filedata.replace('__schema__', schema)
    filedata = filedata.replace('__dbuser__', dbuser)
    filedata = filedata.replace('__dbport__', dbport)
    filedata = filedata.replace('__dbname__', dbname)
    filedata = filedata.replace('__password__', dbpassword)
    filedata = filedata.replace('__dbport__', dbport)
    with open(liquibase_folder_path + "liquibase.update.properties", 'w') as file:
        file.write(filedata)
    with open(liquibase_folder_path + "changelog-update.xml", 'r') as file:
        filedata = file.read()
    filedata = re.sub(
        '<includeAll path = ".*"/>',
        '<includeAll path = "file:' + approot + '/WEB-INF/classes/migrations/"/>',
        filedata
    )
    with open(liquibase_folder_path + "changelog-update.xml", 'w') as file:
        file.write(filedata)
    #read the config file
    file = open(liquibase_folder_path + "liquibase.update.properties", 'r')

if environment_type == 'dev' :
    logging.info("Dev environment detected; Command line arguments would be ignored")
    migration_folder_path = 'src/main/resources/migrations/'
    liquibase_folder_path = 'src/main/resources/liquibase/'
    m2_repository_home = '$HOME/.m2/repository'
    liquibase_jar_path = m2_repository_home + '/org/liquibase/liquibase-core/3.5.0/liquibase-core-3.5.0.jar'

    ##install liquibase jar
    subprocess.call(['mvn', '-ntp', 'dependency:get', '-Dartifact=org.liquibase:liquibase-core:3.5.0', '-DrepoUrl'])

    #read config file
    if(os.path.isfile(liquibase_folder_path + "liquibase.properties.config")):
        subprocess.call(['cp', liquibase_folder_path + "liquibase.properties.config", \
                               liquibase_folder_path + "liquibase.properties"])
        file = open(liquibase_folder_path + "liquibase.properties", 'r')
    else:
        logging.error("liquibase.properties.config file missing")
        exit()

config = file.read()
file.close()
properties = config.split('\n')
index = 0

while index < len(properties):
    prop = properties[index]
    prop = prop.split(':')
    if(prop[0] == 'defaultSchemaName'):
        schema = prop[1]
    if(prop[0] == 'url'):
        url = prop[4]
    if(prop[0] == 'username'):
        user = prop[1]
    if(prop[0] == 'password'):
        password = prop[1]
    index += 1
url = url.strip()
dbname = url.rsplit('/', 1)[-1]

#connect to db
try:
    con = psycopg2.connect("dbname=" + dbname + " user=" + user + " port=" + dbport + " host=" + host + " password=" + password)
except:
    logging.error("Unable to connect to the database")
    exit()
cur = con.cursor()
cur.execute("show server_version")
version = cur.fetchall()[0][0]
cur.close()
con.close()
##remove minor versions from the version
version_regex = r"^(\d+\.\d+)(.*)"
subst = "\\1"
version = re.sub(version_regex, subst, version, 0)

liquibase_file = "liquibase.update.properties"
if environment_type == 'dev':
    liquibase_file = "liquibase.properties"

with open(liquibase_folder_path + liquibase_file, 'r') as file:
    filedata = file.read()
if(float(version) >= 10.0):
    postgresdriver = 'postgresql-42.2.14.jar'
else:
    postgresdriver = 'postgresql-9.3-1101.jdbc4.jar'
filedata = filedata.replace('__POSTGRESDRIVER__', postgresdriver)
with open(liquibase_folder_path + liquibase_file, 'w') as file:
    file.write(filedata)

#create schema
try:
    con = psycopg2.connect("dbname=" + dbname + " user=" + user + " port=" + dbport + " host=" + host + " password=" + password)
except:
    logging.error("Unable to connect to the database")
    exit()
logging.info("Connection to db successful")
con.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
cur = con.cursor()
cur.execute('CREATE SCHEMA ' + schema)

#set schema in file named 0.sql
if(os.path.isfile(liquibase_folder_path + "0.sql.config")):
    subprocess.call(['cp', liquibase_folder_path + "0.sql.config", \
                           migration_folder_path + "0.sql"])
    schema_file = open(migration_folder_path + "0.sql", 'a+')
else:
    logging.error("0.sql.config file doesn't exist")
    exit()
schema_file.write("SET SEARCH_PATH TO " + schema)
schema_file.close()

logging.info("Schema precision is " + str(precision))
if precision == 2:
    context = "!precision-3"

if precision == 3:
    context = "precision-3"

#run all migrations
logging.info("Running all migrations")
if environment_type == 'dev' :
    subprocess.check_call(['java -jar ' + liquibase_jar_path + ' --defaultsFile=' + liquibase_folder_path + 'liquibase.properties' + ' --contexts=' + context + ' update'], shell=True)
else : 
     subprocess.check_call(['java -jar ' + liquibase_jar_path + ' --defaultsFile=' + liquibase_folder_path + 'liquibase.update.properties' + ' --contexts=' + context + ' update'], shell=True)
logging.info("DB migration completed")
