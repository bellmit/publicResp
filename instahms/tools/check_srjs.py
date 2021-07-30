#!/usr/bin/env python3

import sys
import io
import os
import demjson
import json

file_name = sys.argv[1]

df = io.open(file_name)
desc_string = df.read()
df.close()
desc = demjson.decode(desc_string)

print("Title: ", desc["title"])
print("Description: ", desc["description"])

#df = io.open(file_name)
#desc = json.load(df, strict=False)

#print "Title: ", desc["title"]
#print "Description: ", desc["description"]

