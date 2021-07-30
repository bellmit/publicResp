import datetime
import glob
import os
import psycopg2
import subprocess

MIGRATION_FOLDER_PATH = 'src/main/resources/migrations/'

MIGRATION_FILE_TEMPLATE = """-- liquibase formatted sql
-- changeset {0}:migrate-{1}-log_id-to-bigint
-- preconditions onFail:MARK_RAN
-- precondition-sql-check expectedResult:integer SELECT data_type FROM information_schema.columns WHERE column_name = 'log_id' and table_name = '{1}' and table_schema = current_schema();

ALTER TABLE {1} RENAME TO {1}_old;
CREATE TABLE {1} (LIKE {1}_old INCLUDING ALL);
ALTER TABLE {1} ALTER COLUMN log_id TYPE bigint;
COMMENT ON TABLE {1} IS '{2}';
"""

SELECT_ALL_AUDIT_LOG_TABLES = """
SELECT DISTINCT table_name FROM information_schema.tables WHERE table_name LIKE '%\_audit_log';
"""

SELECT_TABLE_COMMENT = """
SELECT obj_description('{0}.{1}'::regclass, 'pg_class');
"""

def create_migration_file(tablename, tablecomment):
  timestamp = str(datetime.datetime.now().strftime("%Y%m%d%H%M"))
  filename = "{0}-migrate-{1}-log_id-to-bigint".format(timestamp, tablename)
  f = open(MIGRATION_FOLDER_PATH + filename + '.sql','w+')

  username = subprocess.check_output(['git', 'config', '--global', 'user.name'])
  username = username.decode()[:-1]
  username = username.lower()
  username = username.replace(" ", "")
  f.write(MIGRATION_FILE_TEMPLATE.format(username, tablename,  tablecomment))
  f.close()
  print('Created file: ' + MIGRATION_FOLDER_PATH + filename + '.sql')

def create_migration_files_for_new_audit_log_tables(schema_name):
    try:
        con = psycopg2.connect(
            "dbname=hms user=postgres password="
        )
    except BaseException:
        print("Unable to connect to the database")
        sys.exit(1)

    cur = con.cursor()

    cur.execute(SELECT_ALL_AUDIT_LOG_TABLES)
    tablenames = [table[0] for table in cur.fetchall()]

    for table in tablenames:
      migrations_for_table = glob.glob("{}*{}*".format(MIGRATION_FOLDER_PATH, table))
      if not migrations_for_table:
        cur.execute(SELECT_TABLE_COMMENT.format(schema_name, table))
        comment = [row[0] for row in cur.fetchall()]
        create_migration_file(table, comment[0])

    cur.close()
    con.close()

if __name__ == '__main__':
  create_migration_files_for_new_audit_log_tables('test_schema')
