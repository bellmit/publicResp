import re
import datetime
import os
import argparse
import subprocess

parser = argparse.ArgumentParser(description='Generate empty file for liquibase migration')
parser.add_argument('filecontext', type=str, help='appended in the filename after hyphen')
args = parser.parse_args()
migration_folder_path = 'src/main/resources/migrations/'
#get current datetime
slug = re.sub('[^a-z0-9]+',"-",args.filecontext.lower())
filename = str(datetime.datetime.now().strftime("%Y%m%d%H%M")) + "-" + slug

#create file
f = open('src/main/resources/migrations/' + filename + '.sql','w+')
f.write("-- liquibase formatted sql\n")

#get username
username = subprocess.check_output(['git', 'config', '--global', 'user.name'])
username = username.decode()[:-1]
username = username.lower()
username = username.replace(" ", "")
f.write("-- changeset " + username + ":" + slug)
f.close()
print('Created file: ' + migration_folder_path + filename + '.sql')

