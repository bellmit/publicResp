import logging
import psycopg2
import os

logging.basicConfig(
    format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s',
    filename='/var/log/insta/minio_migrate.log',
    level=logging.DEBUG,
    datefmt='%d/%m/%Y %I:%M:%S %p'
)

OPTIONS_FILE = "/etc/hms/options"
user = "postgres"


def read_minio_config_from_environment_properties():
    path_to_environment_properties = '/root/webapps/instahms/WEB-INF/classes/java/resources/environment.properties'
    #read the config file
    file = open(path_to_environment_properties, 'r')
    config = file.read()
    file.close()
    env_props = config.split('\n')
    index = 0
    properties = {}
    while index < len(env_props):
        prop = env_props[index].split('=')
        prop_key = prop[0]
        prop_val = '='.join(prop[1:])
        if(prop_key == 'minio.url'):
            url_parts = prop_val.split('://')
            secure = False
            if url_parts[0] == 'https':
                secure = True
            properties['secure'] = secure
            properties['endpoint'] = url_parts[1]
        if(prop_key == 'minio.region'):
            properties['region'] = prop_val
        if(prop_key == 'minio.access.key'):
            properties['access_key'] = prop_val
        if(prop_key == 'minio.secret.key'):
            properties['secret_key'] = prop_val
        if(prop_key == 'minio.documents.bucket.name'):
            properties['bucket_name'] = prop_val
        index += 1
    return properties

def get_db_host():
    host = ""
    if (os.path.isfile(OPTIONS_FILE)):
        options_file_content = open(OPTIONS_FILE).readlines()
        for line in options_file_content:
            if 'DBHOST' in line and "#" not in line:
                host = line.split("=")[1].strip()
    if host == "":
        host = 'localhost'
    return host

def get_db_port():
    port = ""
    if (os.path.isfile(OPTIONS_FILE)):
        options_file_content = open(OPTIONS_FILE).readlines()
        for line in options_file_content:
            if 'DBPORT' in line and "#" not in line:
                port = line.split("=")[1].strip()
    if port == "":
        port = '5432'
    return port

def get_db_password():
    password = ""
    if (os.path.isfile(OPTIONS_FILE)):
        options_file_content = open(OPTIONS_FILE).readlines()
        for line in options_file_content:
            if 'DBPASSWORD' in line and "#" not in line:
                dbpassword = line.split("=")[1].strip()
    return password

password = get_db_password()
ipaddress = get_db_host()
dbport = get_db_port()

def get_db_connection(dbname):
    try:
        db_connection = psycopg2.connect(dbname=dbname, user=user, host=ipaddress, port=dbport, password=password)
    except:
        logging.error("Unable to connect to the database with dbname:" + dbname + " host:" + ipaddress)
        exit()
    return db_connection

def set_search_path(cursor, schema):
    cursor.execute("SET SEARCH_PATH TO " + schema)
    return cursor

def execute_query(query, params, dbname, schema):
    db_connection = get_db_connection(dbname)
    cursor = set_search_path(db_connection.cursor(), schema)
    cursor.execute(query, params)
    cursor.close()
    db_connection.commit()
    db_connection.close()

query_get_all_databases = "SELECT datname FROM pg_database WHERE datistemplate = false"

def get_all_databases():
    database_list = []
    try:
        db_connection = psycopg2.connect(user=user, host=ipaddress, port=dbport, password=password)
    except:
        logging.error("Unable to connect to the database with host:" + ipaddress)
        exit()
    cur = db_connection.cursor()
    cur.execute(query_get_all_databases)
    HOSTNAME_ENDSWITH_BLACKLIST = ("_t", "_pr", "_test", "pr", "test", "_q")
    for record in cur:
        if (record[0].startswith('hms') and 
            not record[0].endswith(HOSTNAME_ENDSWITH_BLACKLIST)):
            database_list.append(record[0])
    cur.close()
    db_connection.close()
    return database_list

query_get_all_schemas = (
    "SELECT nspname as schema FROM pg_catalog.pg_namespace WHERE nspname "
    "NOT LIKE '%\\_g' AND nspname NOT LIKE '%\\_report' AND nspname "
    "NOT LIKE '%\\_i' AND nspname NOT LIKE '%\\_demo' AND nspname "
    "NOT LIKE '%\\_stk' AND nspname NOT LIKE '%\\_s' AND nspname "
    "NOT LIKE '%\\_test' AND nspname NOT LIKE '%\\_orig' AND nspname "
    "NOT LIKE '%\\_old' AND nspname NOT LIKE 'pg\\_%' AND nspname "
    "NOT LIKE '%\\_temp' AND nspname NOT IN "
    "('public', 'information_schema', 'extensions', 'test')"
)

def get_all_schemas(dbname):
    schema_list = []
    db_connection = get_db_connection(dbname)
    cur = db_connection.cursor()
    cur.execute(query_get_all_schemas)
    for record in cur:
        schema_list.append(record[0])
    return schema_list

