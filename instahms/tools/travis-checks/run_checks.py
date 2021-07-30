import os
import sys

from check_audit_logs_log_id_column_type import check_for_invalid_audit_log_log_id_column_type
from check_for_logger import uses_older_logger
from check_for_precision_changes import has_new_numeric_columns
from validate_db_comments import are_db_comments_valid

if os.environ['TRAVIS_EVENT_TYPE'] != 'pull_request':
        exit(0)

checks_passed = True

has_new_numeric_columns()
checks_passed = checks_passed and are_db_comments_valid()
checks_passed = checks_passed and check_for_invalid_audit_log_log_id_column_type()
checks_passed = checks_passed and not uses_older_logger()

if not checks_passed:
        sys.exit(1)