Purpose
=======
This README document describes how to deal with changes to the Insta HMS
schema definition as well as initial data.

The following files are used to create/update the schema:

1. init.sql: main file containing all the table definitions, sequences,
   constraints. This represents the schema AS OF PREVIOUS MAJOR VERSION,
   including the initial data.

2. db_changes.sql: running file of changes over and above, dealing with all
   the changes to tables as well as migration of data from the previous major
   version till now.

3. vft.sql: This contains the definition of all views, functions and triggers
   AS OF NOW. Since all views/functions/triggers are data independent, we
   can always drop these and re-create whenever we want. So, as a convenience,
   this is separated out from init.sql.

Editing files: Normal trunk development
=======================================
init.sql
--------
Never edit this file for regular development. See the section on 
Merging db_changes to trunk for an explanation of why.

db_changes.sql
--------------
This is a running set of statements for changes to an existing schema. Since
this includes both previous major versions, and previous minor versions, some
rules need to be followed:

a. Always APPEND to db_changes.sql. This is because, you must assume that any
   statements that are already there in this file may have go executed in some
   schema or the other. During development phase, this is mostly true for all
   developers' local schemas.

b. If you must make a change to an existing statement due to an error, do not
   delete it. Comment it out so that the number of lines does not change.

c. When dropping/altering a column or a table, some views may be associated
   the table. You should drop all such views prior to altering the table or
   column. You may also use the CASCADE keyword instead. Note that since
   vft.sql is always run after db_changes, it is safe and OK to drop any
   views in db_changes.sql.

d. Any changes to views/functions/triggers should NOT be done in db_changes.sql

e. Always test it: the best way is to FIRST write the db_changes.sql changes,
   and then copy/paste it to psql or pgAdmin. 

vft.sql
-------
This is NOT a running set of statements. This reflects the CURRENT status of
all views, functions and triggers. When modifying one of these, directly modify
it in the file.

At any point in time, it should be OK to run and re-run vft.sql.

Editing files: branch development
=================================
The process is different for branch development, since any changes to the
branch affects the PREVIOUS MAJOR VERSION. Thus, the following must be done:

1. In branch, append to db_changes.sql as usual

2. In trunk: append to db_changes, but prefix it with "--_INCR_ONLY_ "

This means that the statement will be included only when running an
incremental upgrade from an existing trunk version to a new trunk version.
When a fresh schema is created, or when upgrading from previous version, the
statement is not run (since it is already included in the previous version)

3. In trunk, init.sql needs regeneration if it is a schema change (as
opposed to an update/insert). See below for Merging Changes to trunk,
for steps to regenerate init.sql. Or, init.sql can be manually edited.

In effect, what you are doing on trunk is to say "previous version has
changed", and that is why init.sql is changed and not db_changes.

How to create a local schema
============================
From the bin directory, run "./new_schema.sh". This will create the local
schema for you, provided you have set up postgres correctly.

What happens during upgrade
===========================
There are two scenarios to be handled:

(a) Major version upgrade eg, 5.1 to 5.2 (Full migration)

We run db_changes.sql on the existing schema, followed by vft.sql. Since
db_changes.sql contains all the changes to current version from previous
major version, this will alter tables and migrate data.

(b) Minor version upgrade eg, 5.1.1 to 5.1.2 (Incremental migration)

We look for the tag of the previous version in db_changes.sql. All statements
AFTER this tag are run on the schema. This is followed by running vft.sql.
This is the reason why db_changes lines should not be modified, and lines should
always be APPENDED to it.

New schema creation
===================
The following scripts are run in sequence:

1. init.sql: Now, we have a schema reflecting previous version
2. db_changes.sql: Converts previous major version to current version
3. vft.sql: reinstalls all views/functions/triggers

Merging db_changes to trunk
===========================
Whenever we release a major version, we branch out the SVN repository. On
trunk, we re-initialize init.sql and master_misc.sql to reflect the new
previous version.

1. Ensure the branch is checked out with the latest

  svn update

2. new_schema fresh (on branch, say, in db called hms90)

  ./new_schema.sh fresh

3. Truncate the tables which contain uploaded forms (we upload them
separately, not part of init.sql, to keep init.sql very light)

  cat << EOF | psql -d hms90 
set search_path to fresh;
delete from registration_cards;
EOF

4. Dump the schema on trunk withink install/sql directory:

  pg_dump hms90 -n fresh --no-owner -f init.sql

5. Remove all functions, views and triggers and search path in init.sql and
remove create schema and set search path:

     sed --in-place -e '/Type: VIEW/,+7d' init.sql
     sed --in-place -e '/CREATE FUNCTION/,/SET default_tablespace/d' init.sql
     sed --in-place -e '/CREATE TRIGGER/,/FK CONSTRAINT/d' init.sql
     sed --in-place -e '/CREATE SCHEMA/,/SET search_path/d' init.sql

6. Create a new empty db_changes_90_91.sql

7. Test using new_schema.sh.

8. Update upgrade_vars in WEB-INF/install to reflect the previous
   version number and scripts required.

