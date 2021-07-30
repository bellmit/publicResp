import subprocess
import psycopg2
import os
import csv
import sys
import argparse
import logging
import requests

OPTIONS_FILE = "/etc/hms/options"

parser = argparse.ArgumentParser(description='Liquibase update for a given schema and database')
parser.add_argument('schema', type=str, help='Postgresql schema name')
parser.add_argument('dbname', type=str, help='Postgresql db name')
parser.add_argument('leads_csv', type=str, help='Leads CSV File')
parser.add_argument('report_recipient', type=str, help='Report Recipient')
parser.add_argument('mailgun_domain', type=str, help='Mailgun API Domain')
parser.add_argument('mailgun_api_key', type=str, help='Mailgun API Key')

args = parser.parse_args()
schema = args.schema
dbname = args.dbname
leads_csv_path = args.leads_csv
report_recipient = args.report_recipient
mailgun_api_key = args.mailgun_api_key
mailgun_domain = args.mailgun_domain

bill_output_csv = "/tmp/%s_bill_book_attribution.csv" % (schema)
uhid_output_csv = "/tmp/%s_uhid_book_attribution.csv" % (schema)

logging_directory = '/tmp'
if not os.path.exists(logging_directory):
    os.makedirs(logging_directory)

logging.basicConfig(format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s', filename=logging_directory + '/book_attribution_by_phone.log',
                    level=logging.DEBUG, datefmt='%d/%m/%Y %I:%M:%S %p')

if (not os.path.exists(leads_csv_path)):
    sys.exit("File not found: %s" % (leads_csv_path))

#read db host and db password from /etc/hms/options
if dbname != "hms":
        options_file = OPTIONS_FILE + "." + dbname
host=""
dbpassword=""
if (os.path.isfile(OPTIONS_FILE)):
    options_file_content = open(OPTIONS_FILE).readlines()
    for line in options_file_content:
        if 'DBHOST' in line and "#" not in line:
            host = line.split("=")[1].strip()
        if 'DBPASSWORD' in line and "#" not in line:
            dbpassword = line.split("=")[1].strip()

if host == "":
    host = 'localhost'
logging.info("Host detected as:" + host + " Password detected as:" + dbpassword)

con_str = "dbname=" + dbname + " user=postgres" + " host=" + host
if dbpassword != "":
    con_str = con_str + " password=" + dbpassword
#con.ct to db
try:
    con = psycopg2.connect(con_str)
except:
    logging.error("Unable to connect to the database")
    exit()


#create schema
logging.info("Connection to db successful")
cur = con.cursor()
cur.execute("DROP TABLE IF EXISTS %s.tmp_book_attribution_bill_export" % (schema))
cur.execute("DROP TABLE IF EXISTS %s.tmp_book_attribution_uhid_export" % (schema))
cur.execute("DROP TABLE IF EXISTS %s.tmp_book_attribution_csv_import" % (schema))
cur.execute("CREATE TABLE %s.tmp_book_attribution_csv_import (communication_date TIMESTAMP WITHOUT TIME ZONE, phone_number CHARACTER VARYING(20), patient_name CHARACTER VARYING(500), email CHARACTER VARYING(500), local_number CHARACTER VARYING(20), national_number CHARACTER VARYING(20))" % (schema))
cur.execute("CREATE TABLE %s.tmp_book_attribution_uhid_export (mr_no CHARACTER VARYING(15), patient_name CHARACTER VARYING(500), patient_phone CHARACTER VARYING(16), email_id CHARACTER VARYING(500), last_visit_date_before_communication_date TIMESTAMP WITHOUT TIME ZONE, communication_date TIMESTAMP WITHOUT TIME ZONE, elgible_for_attribution CHAR(1), CONSTRAINT tmp_book_attribution_uhid_export_pk PRIMARY KEY (mr_no))" % (schema))
cur.execute("CREATE TABLE %s.tmp_book_attribution_bill_export (mr_no CHARACTER VARYING(15), visit_id CHARACTER VARYING(20), visit_date TIMESTAMP WITHOUT TIME ZONE, visit_type CHAR(2), bill_no CHARACTER VARYING(20), open_date TIMESTAMP WITHOUT TIME ZONE, total_amount numeric(15,2), status CHARACTER VARYING(15), CONSTRAINT tmp_book_attribution_bill_export_pk PRIMARY KEY (bill_no))" % (schema))
cur.execute("CREATE INDEX tmp_book_attribution_csv_import_phone_number_idx ON %s.tmp_book_attribution_csv_import(phone_number)" % (schema))
cur.execute("CREATE INDEX tmp_book_attribution_csv_import_local_number_idx ON %s.tmp_book_attribution_csv_import(local_number)" % (schema))
cur.execute("CREATE INDEX tmp_book_attribution_csv_import_national_number_idx ON %s.tmp_book_attribution_csv_import(national_number)" % (schema))
cur.execute("CREATE INDEX tmp_book_attribution_csv_import_communication_date_idx ON %s.tmp_book_attribution_csv_import(communication_date)" % (schema))
cur.execute("CREATE INDEX tmp_book_attribution_uhid_export_elgible_for_attribution_idx ON %s.tmp_book_attribution_uhid_export(elgible_for_attribution)" % (schema))
con.commit()
logging.info("importing leads csv")
with open(leads_csv_path, 'r') as f:
    reader = csv.reader(f)
    next(reader)  # Skip the header row.
    for row in reader:
        cur.execute("INSERT INTO " + schema + ".tmp_book_attribution_csv_import(communication_date,phone_number,patient_name,email) VALUES (%s, %s, %s, %s)", row)
logging.info("Leads data imported")
con.commit()

cur.execute("UPDATE %s.tmp_book_attribution_csv_import SET local_number = replace(phone_number, '+91', ''), national_number = replace(phone_number, '+91', '0')" % (schema)) 
logging.info("local_number and national_number updated in leads table")

cur.execute("INSERT INTO %s.tmp_book_attribution_uhid_export SELECT pd.mr_no, CONCAT_WS(' ', pd.patient_name, pd.middle_name, pd.last_name) AS patient_name, pd.patient_phone, pd.email_id, prlast.last_visit_date_before_communication_date, bacsv.communication_date, 'N' AS elgible_for_attribution FROM %s.patient_details pd JOIN %s.tmp_book_attribution_csv_import bacsv ON bacsv.phone_number = pd.patient_phone OR bacsv.local_number = pd.patient_phone OR bacsv.national_number = pd.patient_phone LEFT JOIN (SELECT pr.mr_no, max(pr.reg_date + pr.reg_time) as last_visit_date_before_communication_date FROM %s.patient_details pd1 JOIN %s.tmp_book_attribution_csv_import bacsv1 ON bacsv1.phone_number = pd1.patient_phone OR bacsv1.local_number = pd1.patient_phone OR bacsv1.national_number = pd1.patient_phone JOIN %s.patient_registration pr ON ((pr.reg_date + pr.reg_time) < bacsv1.communication_date) AND pr.mr_no = pd1.mr_no GROUP BY pr.mr_no) prlast ON prlast.mr_no = pd.mr_no" % (schema, schema, schema, schema, schema, schema))

logging.info("Exported mr_no matching phone number AND last visit date (if any) before communcation date")

cur.execute("UPDATE %s.tmp_book_attribution_uhid_export SET elgible_for_attribution = 'Y' WHERE last_visit_date_before_communication_date IS NULL OR last_visit_date_before_communication_date < (communication_date  - INTERVAL '180 DAY')" % (schema))

logging.info("Marked mr_no as book attribution elgible which meet elgibility criteria")

cur.execute("INSERT INTO %s.tmp_book_attribution_bill_export (SELECT pr.mr_no, b.visit_id, (pr.reg_date + pr.reg_time) AS visit_date, CASE WHEN pr.visit_type = 'o' THEN 'OP' ELSE 'IP' END, b.bill_no, b.open_date, b.total_amount, CASE WHEN b.status = 'A' THEN 'Open' WHEN b.status = 'F' THEN 'Finalized' WHEN b.status = 'S' THEN 'Settled' WHEN b.status = 'C' THEN 'Closed' ELSE NULL END FROM %s.bill b JOIN %s.patient_registration pr ON b.visit_id = pr.patient_id JOIN %s.tmp_book_attribution_uhid_export baexp on pr.mr_no = baexp.mr_no AND baexp.elgible_for_attribution = 'Y' WHERE b.open_date BETWEEN baexp.communication_date AND (communication_date + INTERVAL '180 DAY'))" % (schema, schema, schema, schema))

logging.info("Exported all bills details for elgible mr_nos to temp table")
con.commit()
with open(uhid_output_csv, 'w') as f:
    csvwriter = csv.writer(f)
    csvwriter.writerow(["mr_no", "patient_name", "patient_phone", "email_id", "last_visit_date_before_communication_date", "communication_date", "elgible_for_attribution"])
    cur.execute("select * from %s.tmp_book_attribution_uhid_export" % (schema))
    for row in cur:
        csvwriter.writerow(list(row))
logging.info("Exported uhid details to %s" % uhid_output_csv)

with open(bill_output_csv, 'w') as f:
    csvwriter = csv.writer(f)
    csvwriter.writerow(["mr_no", "visit_id", "visit_date", "visit_type", "bill_no", "open_date", "total_amount", "status"])
    cur.execute("select * from %s.tmp_book_attribution_bill_export" % (schema))
    for row in cur:
        csvwriter.writerow(list(row))
    
logging.info("Exported bill details to %s" % uhid_output_csv)

cur.execute("DROP TABLE %s.tmp_book_attribution_bill_export" % (schema))
cur.execute("DROP TABLE %s.tmp_book_attribution_uhid_export" % (schema))
cur.execute("DROP TABLE %s.tmp_book_attribution_csv_import" % (schema))
logging.info("Dropped temporary tables")
con.commit()
cur.close()
con.close()
logging.info("Mailing csv to %s" % (report_recipient))

attachments = []
if os.path.exists(uhid_output_csv):
    attachments.append(("attachment", ("%s_uhid_book_attribution.csv" % (schema), open(uhid_output_csv,"rb").read())))
if os.path.exists(bill_output_csv):
    attachments.append(("attachment", ("%s_bill_book_attribution.csv" % (schema), open(bill_output_csv,"rb").read())))

response = requests.post(
    "https://api.mailgun.net/v3/%s/messages" % (mailgun_domain),
    auth=("api", mailgun_api_key),
    files=attachments,
    verify=False,
    data={"from": "NoReply <no-reply@%s>" % (mailgun_domain),
          "to": report_recipient,
          "subject": "Book Attribution Reports - Insta HMS (%s)" % (schema),
          "text": "Hi Team, \n Find attached reports for Insta HMS Schema %s" % (schema),
          "html": "<html><body><p>Hi Team,</p><p>Find attached reports for Insta HMS Schema %s</p></body></html>" % (schema)})
if response.status_code == 200:
    logging.info("Mail Sent to %s" % (report_recipient))
else:
    logging.info("Mail Sending failed to %s" % (report_recipient))
    logging.info(response.json())

