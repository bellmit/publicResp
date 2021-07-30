import time
import sys
import requests
import json
import subprocess
import os
import argparse
import datetime
import mimetypes

def get_mr_nos(file_name):
    with open(file_name, 'r') as f:
        mr_lines = f.readlines()
        return [line.strip() for line in mr_lines]

parser = argparse.ArgumentParser(description='EMR Data Exporter for Insta HMS')
parser.add_argument('host', type=str, help='host name')
parser.add_argument('appname', type=str, help='app name like hms, hmspr')
parser.add_argument('schema', type=str, help='schema name')
parser.add_argument('user_id', type=str, help='application login name')
parser.add_argument('password', type=str, help='application login password')
parser.add_argument('mr_nos_file', type=str, help='mr number file')
parser.add_argument('out_folder', type=str, help='output folder')

args = parser.parse_args()

cpath = "http://" + args.host + "/insta" + args.appname
emr_url = cpath + "/emr/EMRMainDisplay.do?_method=list&mr_no="

mr_nos = get_mr_nos(args.mr_nos_file)
out_folder = args.out_folder

creds = {
    "hospital" : args.schema,
    "userId" : args.user_id,
    "password": args.password
}
print("Fetching Login Page")
session = requests.Session()
cr = session.get(cpath + "/loginForm.do") 
cookies = session.cookies.get_dict()
print("Performing Login")
r = session.post(cpath + "/login.do", data=creds)
if r.status_code != 200:
    print("Login failed!")
    sys.exit(1) 
for mr_no in mr_nos:
    response = session.get(emr_url + mr_no, cookies=cookies)
    response_content_str = response.content.decode('unicode_escape')
    repsonse_content=[]
    if "\\t" in response_content_str:
        print("splitting condensed")
        response_content = response_content_str.replace("\\t"," ").split("\\n")
    else:
        response_content = response_content_str.split("\n")
    for l in response_content:
        if not l.strip().startswith("var allDocsList = "):
            continue
        docs = json.loads(l.strip().replace("\\'","'")[18:-1])
        if len(docs) <=0:
            print("Empty list for " + mr_no)
            time.sleep(1)
            continue
        doc_list = docs[0]
        mr_folder = os.path.join(out_folder, mr_no)
        subprocess.call(['mkdir', '-p', mr_folder])
        for doc_type_list in doc_list:
            doc_type = doc_type_list["filterId"]
            doc_label = doc_type_list["label"]
            print("Fetching " + doc_label + " for " + mr_no)
            docs = doc_type_list["viewDocs"]
            doc_type_folder = os.path.join(mr_folder, doc_label)
            subprocess.call(['mkdir', '-p', doc_type_folder])
            for doc in docs:
                doc_response = session.get(cpath + doc["displayUrl"], cookies=cookies)
                if doc["date"] is None:
                    doc["date"] = 0.00
                doc_date=datetime.datetime.fromtimestamp(doc["date"]/1000.0)
                doc_title=doc["title"]
                file_ext=None
                if "content-disposition" in doc_response.headers:
                    file_ext="." + doc_response.headers['content-disposition'].split("=")[-1].strip("\"").split(".")[-1]
                else:
                    file_ext=mimetypes.guess_extension(doc_response.headers['content-type'].split(";")[0], True)
                if file_ext is None:
                    file_ext = ".pdf"
                with open(os.path.join(doc_type_folder, mr_no + "_" + doc_date.strftime('%Y-%m-%d') +"_" + doc_title.replace('/', '_')[:220] + file_ext), 'wb') as f:
                    f.write(doc_response.content)
    print("Documents exported for " + mr_no)
    time.sleep(1)

