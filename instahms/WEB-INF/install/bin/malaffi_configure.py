import json
import psycopg2
import argparse

parser = argparse.ArgumentParser(description='Liquibase update for a given schema and database')
parser.add_argument('dbname', type=str, help='Postgresql db name')
parser.add_argument('schema', type=str, help='Postgresql schema name')
parser.add_argument('postgresip', type=str, help='Root of the HMS application')

args = parser.parse_args()
dbname = args.dbname
schema = args.schema
if args.postgresip == "localhost":
    postgresIp = "127.0.0.1"
else:
    postgresIp = args.postgresip

# Read json file
with open('malaffi_dataset/malaffi-configure.json') as f:
    data = json.load(f)
    ip_address = data['IP_ADDRESS']
    port = data['PORT_NUMBER']
    send_fac = data['SENDING_FACILITY']
    if data['SENDING_APPICATION'] == "":
        send_app = send_fac
    else:
        send_app = data['SENDING_APPICATION']
    rec_fac = data['RECEIVING_FACILITY']
    if data['RECEIVING_APPICATION'] == "":
        rec_app = data['RECEIVING_APPICATION']
    else:
        rec_app = data['RECEIVING_APPICATION']

#connect to db
try:
    print ("This script deletes existing data from interface_hl7, interface_details_hl7, message_mapping_hl7 tables")
    yourConsent = input("Is this ok...? (ok/no) ")
    if yourConsent == "ok":
        print ("You have accepted to delete the existing records.")
        con = psycopg2.connect("dbname=" + dbname + " user=postgres host=" + postgresIp + " password= ")
        con.autocommit = True
        cur = con.cursor()
        cur.execute("SET SEARCH_PATH TO " + schema)

        with open("malaffi_dataset/malaffi-configuration-cleanup.sql","r") as f:
            for query in f.readlines():
                try:
                    cur.execute(query)
                except psycopg2.ProgrammingError as e:
                    print ("Failed to run query"+query)
            print ("Existing configurations are cleaned.")

        # Setting configuration in interface_hl7 and interface_details_hl7 tables.
        print ("Adding new configurations as specified in malaffi-configure.json file")
        query = "INSERT INTO interface_hl7 (ip_address,port,code_systems_id) VALUES ('"+ip_address+"','"+port+"',1)"
        cur.execute(query)
        query = "INSERT INTO interface_details_hl7(sending_facility,sending_application,receving_facility,receving_application) VALUES ('"+send_fac+"','"+send_app+"','"+rec_fac+"','"+rec_app+"')"
        cur.execute(query)

        # Message mapping configuration message_mapping_table
        print ("Adding message mapping to table message_mapping_hl7 table.")
        with open("malaffi_dataset/message_mapping.sql", "r") as f:
            for query in f.readlines():
                try:
                    cur.execute(query)
                except psycopg2.ProgrammingError as e:
                    print ("Failed to run query"+query)
        con.close()
    else:
        print ("You have not accepted to delete the records in tables, So aborting the script.")
except psycopg2.ProgrammingError as e:
    print ("Unable to connect to the database with dbname:" + dbname + " host:" + postgresIp)
    print ("Exception :"+e)