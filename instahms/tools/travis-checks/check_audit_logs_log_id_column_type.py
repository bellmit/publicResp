import psycopg2
import sys

from utils import request_review

SELECT_NON_BIGINT_AUDIT_LOG_TABLES = """
SELECT distinct table_name FROM information_schema.columns
  WHERE column_name = 'log_id' AND data_type != 'bigint'
  AND table_name LIKE '%\_audit_log';
"""


def check_for_invalid_audit_log_log_id_column_type():
    issues_found = []

    try:
        con = psycopg2.connect(
            "dbname=hms user=postgres password="
        )
    except BaseException:
        print("Unable to connect to the database")
        sys.exit(1)

    cur = con.cursor()

    cur.execute(SELECT_NON_BIGINT_AUDIT_LOG_TABLES)
    tables = [table[0] for table in cur.fetchall()]

    for table in tables:
        issues_found.append(
          'Table `{}` does not have `bigint` data type for `log_id` column'.format(
            table
          )
        )
        print(table)

    cur.close()
    con.close()

    if tables:
        request_review('<br><br>'.join(issues_found))
        return False

    return True
