import subprocess
import psycopg2
import time
import datetime
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import os

liquibase_folder_path = 'src/main/resources/liquibase/'
migration_folder_path = 'src/main/resources/migrations/'
m2_repository_home = '$HOME/.m2/repository'
liquibase_jar_path = 'org/liquibase/liquibase-core/3.5.0/liquibase-core-3.5.0.jar'

if(os.path.isfile(liquibase_folder_path + "liquibase.properties")):
	file = open(liquibase_folder_path + "liquibase.properties", 'r')
else:
	print("liquibase.properties file missing")
	exit()

if(os.path.isfile(liquibase_folder_path + 'liquibase.test.properties')):
	testfile = open(liquibase_folder_path + "liquibase.test.properties", 'a')
else:
	print("liquibase.test.properties file missing")
	exit()

print("Found liquibase files")

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
	if(prop[0] == 'referenceUsername'):
		testuser = prop[1]
	if(prop[0] == 'referencePassword'):
		testpassword = prop[1]
	index += 1

schema = schema.strip()
user = user.strip()
testuser = testuser.strip()
testpassword = testpassword.strip()
password = password.strip()
url = url.strip()
dbname = url.rsplit('/', 1)[-1]

#connect to db
try:
    con = psycopg2.connect("dbname=" + dbname + " user=" + user + " host='localhost' password=" + password)
except:
    print("Unable to connect to the database")
    exit()

print("Connection to dev database successful")
#create test db
testdbname = 'test'
con.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
cur = con.cursor()
cur.execute('CREATE DATABASE ' + testdbname)
print("Created database " + testdbname)
cur.close()
con.close()
#create test schema
try:
    testcon = psycopg2.connect("dbname=" + testdbname + " user=" + testuser + " host='localhost' password=" + testpassword)
except:
    print("Unable to connect to the test database")
    exit()
print("Connection to test db successful")
testcon.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
testcur = testcon.cursor()
testcur.execute('CREATE SCHEMA ' + schema)
print("Created schema " + schema +  " in database " + testdbname)
testcur.close()
testcon.close()

#set schema in file named 0.sql
if(os.path.isfile(liquibase_folder_path + "0.sql.config")):
	subprocess.call(['cp', liquibase_folder_path + "0.sql.config", \
						   migration_folder_path + "0.sql"])
	schema_file = open(migration_folder_path + "0.sql", 'a+')
else:
	print("0.sql.config file doesn't exist")
	exit()
schema_file.write("SET SEARCH_PATH TO " + schema)
schema_file.close()

#run all migrations
print("Running all migrations")
liquibase_jar_file_path = m2_repository_home + '/' + liquibase_jar_path
try:
	subprocess.call(['java -jar ' + liquibase_jar_file_path + ' --defaultsFile=' + liquibase_folder_path + 'liquibase.test.properties' + ' update'], shell=True)
except subprocess.CalledProcessError as e:
    print(e.output)
    exit()
print("Test db Migration completed")

#generate diff
#get current datetime
filename = str(datetime.datetime.now().strftime("%Y%m%d%H%M"))

#create file
f = open('src/main/resources/migrations/' + filename + '.xml','w+')
f.close()
print('Created file ' + filename + '.xml in ' + migration_folder_path)
#specifiy changelog file in liquibase.properties
file = open(liquibase_folder_path + "liquibase.properties", 'a+')
file.write('changeLogFile:src/main/resources/migrations/' + filename + ".xml")
file.close()

## check liquibase tables if they exist
try:
   con = psycopg2.connect("dbname=" + dbname + " user=" + user + " host='localhost' password=" + password)
except:
   print("Unable to connect to the database")
   exit()
cur = con.cursor()
cur.execute("SELECT EXISTS (SELECT 1 FROM   pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE  n.nspname = '" + schema + "' AND c.relname = 'databasechangelog' AND c.relkind = 'r')")  
result = cur.fetchall()
cur.close()
con.close()
devdbstate=''
if(result[0][0] == True):
	# check liquibase tables to ensure that dev db is upto date
	try:
	   con = psycopg2.connect("dbname=" + dbname + " user=" + user + " host='localhost' password=" + password)
	except:
	   print("Unable to connect to the database")
	   exit()
	cur = con.cursor()
	cur.execute("SET SEARCH_PATH to " + schema)
	time.sleep(0.1)
	cur.execute("SELECT * from databasechangelog order by orderexecuted desc limit 1")
	devdbstate = cur.fetchall()[0][0]
	cur.close()
	con.close()

try:
    testcon = psycopg2.connect("dbname=" + testdbname + " user=" + testuser + " host='localhost' password=" + testpassword)
except:
    print("Unable to connect to the test database")
cur = testcon.cursor()
cur.execute("SET SEARCH_PATH to " + schema)
time.sleep(0.1)
cur.execute("SELECT * from databasechangelog order by orderexecuted desc limit 1")
testdbstate = cur.fetchall()[0][0]
cur.close()
testcon.close()

if(devdbstate!='' and testdbstate!=devdbstate):
	print("Exiting because devdb is not up to date with all migrations")
	exit()

print('Writing diff to ' + filename + '.xml')
#generate diff
subprocess.call(['java -jar ' + liquibase_jar_file_path + ' --defaultsFile=' + liquibase_folder_path + "liquibase.properties" + ' diffChangeLog'], shell=True)
print('Diff generation completed')

#copy liquibase tables if they dont exist
if(result[0][0] == False):
	#copy liquibase tables from test db to new db
	print("Liquibase tables not found; Copying tables from testdb")
	subprocess.call(['pg_dump -d' + ' test' + ' -t '+ schema + '.databasechangeloglock' + ' -t ' + schema +'.databasechangelog -U ' + testuser + ' | psql -U' + user + ' -d' + dbname], shell = True)

#fix generated file
with open(migration_folder_path + filename + '.xml', 'r') as diff:
	diffdata = diff.read()
	diffdata = diffdata.replace('onlyIfExists="true"', '')
with open(migration_folder_path + filename + '.xml', 'w') as diff:
	diff.write(diffdata)

# drop database
# connect to db
try:
   con = psycopg2.connect("dbname=" + dbname + " user=" + user + " host='localhost' password=" + password)
except:
   print("Unable to connect to the database")
   exit()

# drop test db
testdbname = 'test'
con.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
cur = con.cursor()
cur.execute('DROP DATABASE ' + testdbname)
print("Dropped database " + testdbname)
cur.close()
con.close()
