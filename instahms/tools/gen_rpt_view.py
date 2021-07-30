#!/usr/bin/env python3

import sys
import io
import os
import json

def get_desc(file_name):
    df = io.open(indir + "/" + file_name)
    desc_string = df.read()
    main = json.loads(desc_string)
    df.close()

    # read all includes and process them.
    incNames = main.get("includes")
    if not incNames: return main

    incDescs = []
    for inc in incNames:
        incDescs.append(get_desc(inc))

    # merge the fields
    fields = {}
    # first add all include fields in reverse order
    for desc in reversed(incDescs):
        fields.update(desc["fields"])

    # now add our own fields
    if "fields" in main:
        for name, field in main["fields"].items():
            if (field.get("displayName")):
                fields[name] = field      # replace included, if any
            else:
                fields.pop(name)          # remove if it was there in the include

    # set this as the main's field list
    main["fields"] = fields

    # merge the joinTables
    if not "queryUnits" in main:
        return main

    for qu in main["queryUnits"]:
        remainingNames = list(incNames)
        remainingDescs = list(incDescs)
        newJts = []

        for jt in qu["joinTables"]:
            if "includeName" in jt:
                i = remainingNames.index(jt["includeName"]) # insert included jt here
                remainingNames.pop(i)
                desc = remainingDescs.pop(i)
                newJts.extend(desc["queryUnits"][0]["joinTables"])
            else:
                newJts.append(jt)
        # end for

        for desc in remainingDescs:
            newJts.extend(desc["queryUnits"][0]["joinTables"])       # append at the end

        # this is our new joinTables
        qu["joinTables"] = newJts

    return main

def gen_rpt_view(file_name, out):

    base_name = file_name.replace(".srjs","")
    print(file_name)
    desc = get_desc(file_name)

    if not "title" in desc:
        # is an include srjs, nothing to do
        return

    if not "queryUnits" in desc:
        # nothing do do, no query unit. Must be already a view.
        return

    out.write("--\n-- Generated from " + file_name + "\n--\n")
    out.write("DROP VIEW IF EXISTS rpt_" + base_name.lower() + ";" + "\n")
    out.write("CREATE VIEW rpt_" + base_name.lower() + " AS" + "\n")

    queryUnits = desc["queryUnits"]

    for qi in range(len(queryUnits)):
        queryUnit = queryUnits[qi]

        select = "" if qi == 0 else "UNION ALL\n"
        select += "SELECT"
        out.write(select)
        field_buf = ""

        for f, field in desc["fields"].items():
            # override if any
            if "fieldExpressions" in queryUnit:
                if f in queryUnit["fieldExpressions"]:  
                    field = queryUnit["fieldExpressions"][f]

            if "expression" in field:
                field_expr = field["expression"] + " AS " + f
            elif "expressionLines" in field:
                field_expr = "".join(field["expressionLines"]) + " AS " + f
            else:
                if "table" in field:
                    field_expr = field["table"] + "." + f
                else:
                    field_expr = f
            # select += "debug: " + field["displayName"] + "\n"

            if len(field_buf) + len(field_expr) > 100:
                if field_buf: out.write("  " + field_buf + ",\n")
                field_buf = field_expr
            else:
                if field_buf: field_buf += ", "
                field_buf += field_expr
        # end for

        out.write(field_buf + "\n")

        table = "FROM "
        if "mainTableNameLines" in queryUnit:
            query = "".join(queryUnit["mainTableNameLines"])
        else:
            query = queryUnit["mainTableName"] 
        table += query + " AS " + queryUnit["mainTableAlias"]

        if "joinTables" in queryUnit:
            for join in queryUnit["joinTables"]:
                if not "type" in join:
                    joinType = "LEFT JOIN "
                elif join["type"].upper() == "JOIN":
                    joinType = "JOIN "
                elif join["type"].upper() == "INNER":
                    joinType = "JOIN "
                elif join["type"].upper() == "LEFT":
                    joinType = "LEFT JOIN "
                elif join["type"].upper() == "LEFTLATERAL":
                    joinType = "LEFT JOIN LATERAL"
                elif join["type"].upper() == "JOINLATERAL":
                    joinType = "JOIN LATERAL"
                elif join["type"].upper() == "RIGHT":
                    joinType = "RIGHT JOIN "
                elif join["type"].upper() == "CROSS":
                    joinType = "CROSS JOIN "
                else:
                    joinType = "LEFT JOIN "

                table += "\n" + "  " + joinType + join["name"] + " AS " + join["alias"]
                if joinType != "CROSS JOIN ":
                    table += " " + join["expression"]
            # end for
        # end if

        out.write(table.replace("${prefilter}","") + "\n")

        if "whereExpression" in queryUnit:
            out.write("WHERE " + queryUnit["whereExpression"] + "\n")

    # end for query Units

    out.write(";" + "\n\n")
# end def gen_rpt_view

outfile = sys.argv[1]
indir = sys.argv[2]

print("Creating", outfile, "from", indir)

if outfile == "-":
    out = sys.stdout
else:
    out = open(outfile, mode="w")
out.write("-- liquibase formatted sql\n")
out.write("-- changeset adityabhatia02:gen_report_views.sql runAlways:true\n")
out.write("-- validCheckSum: ANY\n")
if len(sys.argv) > 3:
    gen_rpt_view(sys.argv[3], out)
else:
    for infile in os.listdir(indir):
        if not infile.endswith(".srjs"): continue
        gen_rpt_view(infile, out)

