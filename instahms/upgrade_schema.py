import subprocess
import psycopg2
import os
import re
import sys
import argparse
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
if not os.path.exists(os.path.dirname(logging_directory)):
    os.makedirs(os.path.dirname(logging_directory))

logging.basicConfig(format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s', filename=logging_directory + '/liquibase.log',
                    level=logging.DEBUG, datefmt='%d/%m/%Y %I:%M:%S %p')
logging.debug('schema:' + schema)
logging.info("Approot detected as:" + approot)
liquibase_folder_path = approot + '/WEB-INF/classes/liquibase/'
migration_folder_path = approot + '/WEB-INF/classes/migrations/'
liquibase_jar_path = approot + '/WEB-INF/lib/liquibase-core-3.5.0.jar'

if( not os.path.isfile(liquibase_folder_path + "liquibase.update.properties")):
    sys.stderr.write("LIQUIBASE UPDATE FAILED: liquibase.update.properties file missing\n")
    logging.error("liquibase.update.properties file missing")
    exit()

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
logging.info("DBPORT detected as:" + dbport)

#replace schema and db name in the file
with open(liquibase_folder_path + "liquibase.update.properties", 'r') as file:
    filedata = file.read()
filedata = filedata.replace('__host__', host)
filedata = filedata.replace('__APPROOT__', approot)
filedata = filedata.replace('__schema__', schema)
filedata = filedata.replace('__dbname__', dbname)
filedata = filedata.replace('__dbuser__', dbuser)
filedata = filedata.replace('__dbport__', dbport)
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
config = file.read()
file.close()
properties = config.split('\n')
index = 0
while index < len(properties):
    prop = properties[index]
    prop = prop.split(':')
    if(prop[0] == 'url'):
        ipaddress = prop[3]
        url = prop[4]
    if(prop[0] == 'username'):
        user = prop[1]
    if(prop[0] == 'password'):
        password = prop[1]
    index += 1
    
user = user.strip()
password = password.strip()
url = url.strip()
dbname = url.rsplit('/', 1)[-1]
ipaddress = ipaddress[2:]
logging.info("config file read successfully")

#connect to db
try:
    con = psycopg2.connect("dbname=" + dbname + " user=" + user + " port=" + dbport + " host=" + ipaddress + " password=" + password)
except:
    logging.error("Unable to connect to the database with dbname:" + dbname + " host:" + ipaddress + " port=" + dbport)
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

with open(liquibase_folder_path + "liquibase.update.properties", 'r') as file:
    filedata = file.read()
if(float(version) >= 10.0):
    postgresdriver = 'postgresql-42.2.14.jar'
else:
    postgresdriver = 'postgresql-9.3-1101.jdbc4.jar'
filedata = filedata.replace('__POSTGRESDRIVER__', postgresdriver)
with open(liquibase_folder_path + "liquibase.update.properties", 'w') as file:
    file.write(filedata)


#set schema in file named 0.sql
if(os.path.isfile(liquibase_folder_path + "0.sql.config")):
    subprocess.call(['cp', liquibase_folder_path + "0.sql.config", \
                           migration_folder_path + "0.sql"])
    schema_file = open(migration_folder_path + "0.sql", 'a+')
    logging.info("0.sql updated")
else:
    logging.error("0.sql.config file doesn't exist")
    exit()
schema_file.write("SET SEARCH_PATH TO " + schema + ";\n")
schema_file.close()

#create drop all triggers file
drop_triggers_file_path = '/tmp/drop_triggers.sql'
drop_functions_file_path = '/tmp/drop_functions.sql'

#create drop all views file
drop_views_file_path = '/tmp/drop_views.sql'
drop_triggers_file = open(drop_triggers_file_path, 'w')

try:
    con = psycopg2.connect("dbname=" + dbname + " user=" + user + " port=" + dbport + " host=" + ipaddress + " password=" + password)
except:
    logging.error("Unable to connect to the database with dbname:" + dbname + " host:" + ipaddress + " port=" + dbport)
    exit()
cur = con.cursor()
cur.execute("SELECT 'DROP TRIGGER IF EXISTS ' || trg.tgname || ' ON ' || t.relname || ';' FROM pg_trigger trg JOIN pg_class t ON t.oid= trg.tgrelid JOIN pg_namespace s ON s.nspowner=t.relowner WHERE tgname not ilike '%%ri_constrainttrigger%%' AND s.nspname = '" + schema + "'")
drop_trigger_statements = [i[0] for i in cur.fetchall()]
drop_triggers_file.write("\n".join(drop_trigger_statements))
drop_triggers_file.close()

drop_functions_file = open(drop_functions_file_path, 'w')
cur.execute("SELECT CONCAT_WS('','DROP FUNCTION IF EXISTS ', r.routine_name,'(', string_agg(p.data_type,', ') ,') CASCADE;') AS query FROM information_schema.routines r LEFT JOIN (SELECT * FROM information_schema.parameters ORDER BY ordinal_position) as p ON r.specific_name=p.specific_name WHERE r.specific_schema='" + schema + "' and r.type_udt_name != 'trigger' GROUP BY r.routine_name ORDER BY r.routine_name")
drop_function_statements = [i[0] for i in cur.fetchall()]
drop_functions_file.write("\n".join(drop_function_statements))
drop_functions_file.close()

drop_views_file = open(drop_views_file_path, 'w')
cur.execute("SELECT 'DROP VIEW IF EXISTS ' || table_name || ' CASCADE;' FROM information_schema.views WHERE table_schema = '" + schema + "'")
drop_view_statements = [i[0] for i in cur.fetchall()]
drop_views_file.write("\n".join(drop_view_statements))
drop_views_file.close()
con.close()

#drop all views and triggers
logging.info("Dropping views and triggers")
try:
    con = psycopg2.connect("dbname=" + dbname + " user=" + user + " port=" + dbport + " host=" + ipaddress + " password=" + password)
except:
    logging.error("Unable to connect to the database with dbname:" + dbname + " host:" + ipaddress + " port=" + dbport)
    exit()

cur = con.cursor()
con.autocommit = True
if os.stat("/tmp/drop_triggers.sql").st_size != 0:
    cur.execute("SET SEARCH_PATH TO " + schema)
    with open("/tmp/drop_triggers.sql", "r") as f:
        for query in f.readlines():
            try:
                cur.execute(query)
            except psycopg2.ProgrammingError as e:
                logging.warn("Drop trigger failed statement : " + query)

if os.stat("/tmp/drop_views.sql").st_size != 0:
    cur.execute("SET SEARCH_PATH TO " + schema)
    with open("/tmp/drop_views.sql", "r") as f:
        for query in f.readlines():
            try:
                cur.execute(query)
            except psycopg2.ProgrammingError as e:
                logging.warn("Drop view failed statement : " + query)
con.close()

logging.info("Schema precision is " + str(precision))
if precision == 2:
    context = "!precision-3"

if precision == 3:
    context = "precision-3"

#run liquibase update
logging.info("Liquibase Updating schema:" + schema)
try:
    process = subprocess.Popen(['java -jar ' + liquibase_jar_path + ' --defaultSchemaName=' + schema + ' --defaultsFile=' + liquibase_folder_path \
                                + 'liquibase.update.properties' + ' --contexts=' + context + ' update'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    stdout, stderr = process.communicate()
    if stdout:
        logging.info(stdout)
    if stderr:
        logging.info(stderr)
    exit_code = process.wait()
        
except subprocess.CalledProcessError as e:
    print(e.output)
    exit()

if exit_code == 0:
    logging.info("Liquibase update completed for schema:" + schema)
else:
    logging.error("Liquibase update FAILED for schema:" + schema)
