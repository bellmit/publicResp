import subprocess
import string
import bcrypt
import psycopg2
import os
import re
import sys
import argparse
import logging

def get_pass(password_len=20):
  new_password=None
  symbols='+!'
  chars=string.ascii_lowercase+\
        string.ascii_uppercase+\
        string.digits+\
        symbols

  while new_password is None or \
        new_password[0] in string.digits or \
        new_password[0] in symbols:
     new_password=''.join([chars[ord(os.urandom(1)) % len(chars)] \
                             for i in range(password_len)])
  return new_password


OPTIONS_FILE = "/etc/hms/options"

parser = argparse.ArgumentParser(description='Liquibase update for a given schema and database')
parser.add_argument('schema', type=str, help='Postgresql schema name')
parser.add_argument('dbname', type=str, help='Postgresql db name')

args = parser.parse_args()
schema = args.schema
dbname = args.dbname

logging_directory = os.path.join(os.environ.get('INSTA_LOGPATH','/var/log/insta'), dbname)
if not os.path.exists(os.path.dirname(logging_directory)):
    os.makedirs(os.path.dirname(logging_directory))

logging.basicConfig(format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s', filename=logging_directory + '/password_randomizer.log',
                    level=logging.DEBUG, datefmt='%d/%m/%Y %I:%M:%S %p')
logging.debug('schema:' + schema)

#read db host and db password from /etc/hms/options
if dbname != "hms":
        options_file = OPTIONS_FILE + "." + dbname
host=""
dbpassword=""
user="postgres"
if (os.path.isfile(OPTIONS_FILE)):
    options_file_content = open(OPTIONS_FILE).readlines()
    for line in options_file_content:
        if 'DBHOST' in line and "#" not in line:
            host = line.split("=")[1].strip()
        if 'DBPASSWORD' in line and "#" not in line:
            dbpassword = line.split("=")[1].strip()

if host == "":
    host = '127.0.0.1'
logging.info("Host detected as:" + host + " Password detected as:" + dbpassword)


try:
    if dbpassword != None and dbpassword != '':
      con = psycopg2.connect("dbname=" + dbname + " user=" + user + " host=" + host + " password=" + dbpassword)
    else:
      con = psycopg2.connect("dbname=" + dbname + " user=" + user + " host=" + host)
except:
    logging.error("Unable to connect to the database with dbname:" + dbname + " host:" + host)
    exit()
cur = con.cursor()
cur.execute("SELECT emp_username FROM %s.u_user" % schema)
user_names = [i[0] for i in cur.fetchall()]
#con.autocommit = True
for user_name in user_names:
    password = get_pass()
    password_hashed = bcrypt.hashpw(password, bcrypt.gensalt(prefix=b'2a'))
    logging.info("Randomizing password for " + user_name)    
    cur.execute("UPDATE %s.u_user set emp_password='%s', is_encrypted=true, encrypt_algo='BCRYPT' where emp_username = '%s'" % (schema, password_hashed, user_name))
con.commit()
con.close()
logging.info("Password randomizer executed successfully for schema:" + schema)


