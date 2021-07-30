import json
import psycopg2
import sys

from utils import request_review

SCHEMA_NAME = 'test_schema'

SELECT_COMMENTS_ON_ALL_TABLES = """
SELECT table_name, obj_description(concat('{}.', table_name)::regclass, 'pg_class')
    FROM information_schema.tables WHERE table_schema = '{}' AND table_type = 'BASE TABLE';
""".format(SCHEMA_NAME, SCHEMA_NAME)

SELECT_COMMENTS_ON_ALL_SEQUENCES = """
SELECT sequence_name, obj_description(concat('{}.', sequence_name)::regclass, 'pg_class')
    FROM information_schema.sequences WHERE sequence_schema = '{}';
""".format(SCHEMA_NAME, SCHEMA_NAME)

INVALID_COMMENT_TEMPLATE = "Invalid comment on `{}`"
INVALID_VALUE_FOR_KEY_TEMPLATE = (
    "Invalid value for key `{0}` in comment on `{2}`. {1}"
)
MISSING_COMMENT_TEMPLATE = "Missing comment on `{}`"
MISSING_KEY_IN_COMMENT_TEMPLATE = "Missing key `{}` in comment on `{}`"
OPTIONAL_COMMENT_KEYS = []
REQUIRED_COMMENT_KEYS = ['comment', 'type']
UNKNOWN_KEY_IN_COMMENT_TEMPLATE = "Unknown key `{}` present in comment on `{}`"
VALID_COMMENT_EXAMPLE = (
    "`{ \"type\": \"Txn\", \"comment\": \"Example comment\"}`"
)
VALID_COMMENT_EXAMPLE_MESSAGE = (
    "<br>Comment should be valid a JSON formatted string. "
    "<br>Required keys: ` {} `"
    "<br>Optional keys: ` {} `"
    "<br>Example: {}"
).format(
    ", ".join(REQUIRED_COMMENT_KEYS),
    ", ".join(OPTIONAL_COMMENT_KEYS),
    VALID_COMMENT_EXAMPLE
)

VALID_TABLE_TYPES = ['Master', 'Txn']

ALL_POSSIBLE_KEYS = []
ALL_POSSIBLE_KEYS.extend(OPTIONAL_COMMENT_KEYS)
ALL_POSSIBLE_KEYS.extend(REQUIRED_COMMENT_KEYS)

TABLE_TYPE = 'table'
SEQUENCE_TYPE = 'sequence'

def get_invalid_comment_format_message(tablename):
    return "{}. {}".format(
        INVALID_COMMENT_TEMPLATE.format(tablename),
        VALID_COMMENT_EXAMPLE_MESSAGE
    )

def get_unknown_key_in_comment_message(key, tablename):
    return "{}. {}".format(
        UNKNOWN_KEY_IN_COMMENT_TEMPLATE.format(key, tablename),
        VALID_COMMENT_EXAMPLE_MESSAGE
    )

def get_missing_comment_message(tablename):
    return "{}. {}".format(
        MISSING_COMMENT_TEMPLATE.format(tablename),
        VALID_COMMENT_EXAMPLE_MESSAGE
    )

def get_missing_key_in_comment_message(key, tablename):
    return "{}. {}".format(
        MISSING_KEY_IN_COMMENT_TEMPLATE.format(key, tablename),
        VALID_COMMENT_EXAMPLE_MESSAGE
    )

def get_invalid_value_for_key_message(key, message, tablename):
    return INVALID_VALUE_FOR_KEY_TEMPLATE.format(key, message, tablename)

def comment_row_tuple_to_dict(row):
    try:
        comment = json.loads(row[1], strict=False) # strict=False to allow \n
    except TypeError: # No comment on table/sequence
        comment = None
    except ValueError: # Invalid comment JSON
        comment = {}
    return { "name": row[0], "comment": comment }

def validate_comments(row, row_type):
    tablename = row['name']
    # Check if comment present
    if row['comment'] is None:
        return (False, get_missing_comment_message(tablename))

    # Check if comment is valid JSON
    if len(row['comment'].keys()) == 0:
        return (False, get_invalid_comment_format_message(tablename))

    # Check if comment is valid
    try:
        comment = row['comment']['comment']
        if row_type == TABLE_TYPE and comment.strip() in ('', tablename):
            return (
                False,
                get_invalid_value_for_key_message(
                    'comment',
                    "Comments on a table must describe the table",
                    tablename
                )
            )
    except KeyError:
        return (False, get_missing_key_in_comment_message('comment', tablename))

    # Check if type is valid
    try:
        table_type = row['comment']['type']
        if table_type not in VALID_TABLE_TYPES:
            return (
                False,
                get_invalid_value_for_key_message(
                    'type',
                    "Must be one of: `{}`".format(
                        ", ".join(VALID_TABLE_TYPES)
                    ),
                    tablename
                )
            )
    except KeyError:
        return (False, get_missing_key_in_comment_message('type', tablename))

    # Check for invalid keys
    for key in row['comment'].keys():
        if key not in ALL_POSSIBLE_KEYS:
            return (
                False,
                get_unknown_key_in_comment_message(
                    key,
                    tablename
                )
            )

    return (True, None)

def are_db_comments_valid():
    checks_failed = False
    issues_found = []

    try:
        con = psycopg2.connect(
            "dbname=hms user=postgres password="
        )
    except BaseException:
        print("Unable to connect to the database")
        sys.exit(1)

    cur = con.cursor()

    cur.execute(SELECT_COMMENTS_ON_ALL_TABLES)
    table_comments = map(comment_row_tuple_to_dict, cur.fetchall())

    for comment in table_comments:
        is_valid, error = validate_comments(comment, TABLE_TYPE)
        if not is_valid:
            checks_failed = True
            issues_found.append(error)

    cur.execute(SELECT_COMMENTS_ON_ALL_SEQUENCES)
    sequence_comments = map(comment_row_tuple_to_dict, cur.fetchall())

    for comment in sequence_comments:
        is_valid, error = validate_comments(comment, SEQUENCE_TYPE)
        if not is_valid:
            checks_failed = True
            issues_found.append(error)

    cur.close()
    con.close()

    if checks_failed:
        request_review('<br><br>'.join(issues_found))
        return False

    return True