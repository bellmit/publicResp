#!/usr/bin/env python3
import sys
import json
import os
import csv
import datetime
import re
import logging
import jaydebeapi
import decimal
import fcntl

FEEDBACK_STATUS_COLUMN = "_import_status"
FEEDBACK_DESC_COLUMN = "_import_desc"
PROCESSED_FILE_PREFIX = "_processed_"
QUERY_TIMEOUT = 300 # in seconds
SEQ_NUM = 0
SEQ_PREFIX = datetime.datetime.strftime(datetime.datetime.now(),"%y%m%d%H%M")
JOB_TIME = datetime.datetime.strftime(datetime.datetime.now(),"%d-%m-%Y %H:%M:%S")

def tryLock(fd):
    fcntl.lockf(fd, fcntl.LOCK_SH | fcntl.LOCK_NB)

def unlock(fd):
    fcntl.lockf(fd,fcntl.LOCK_UN)

def _set_stmt_parms(self, prep_stmt, parameters):
        for i, parameter in enumerate(parameters):
            prep_stmt.setObject(i + 1, parameter)
        prep_stmt.setQueryTimeout(QUERY_TIMEOUT)

jaydebeapi.Cursor._set_stmt_parms = _set_stmt_parms

def get_long_uuid():
    global SEQ_NUM
    global SEQ_PREFIX
    SEQ_NUM = SEQ_NUM + 1
    return long(SEQ_PREFIX + str(SEQ_NUM).zfill(4))

def quote(stuff):
    return '\'' + str(stuff) + '\''


def sanitize_object_name(object_name):
    '''
        To handle case sensitive column names in x2insta.
    '''
    return object_name.strip('"').lower()


def encode_row(arr):
    response_arr = []
    for el in arr:
        if el != None and (type(el).__name__ == 'str' or type(el).__name__ == 'unicode'):
            response_arr.append(unicode(el).encode('UTF-8'))
        else:
            response_arr.append(el)
    return response_arr

def decode_row(arr):
    response_arr = []
    for el in arr:
        if el != None and (type(el).__name__ == 'str'):
            response_arr.append(el.decode('utf8'))
        else:
            response_arr.append(el)
    return response_arr


def handle_db_x2insta(dbconfig, entities, integration_config):
    conn = None
    try:
        conn = jaydebeapi.connect(dbconfig['driver_class'],
                                  dbconfig['jdbc_uri'],
                                  [dbconfig['db_username'], dbconfig['db_password']],
                                  dbconfig['classpath'], )

        for entity in entities:
            logging.info("importing entity %s" % (entity))
            table_config = entities[entity]['x_type_config']
            mapper = entities[entity]['mapper']
            table_name = table_config['table']
            table_select = table_config['table_select'] if 'table_select' in table_config.keys() else table_config['table']
            txn_id_column = table_config['txn_id_column']
            status_column = table_config['status_column']
            success_status = eval(table_config['status_success'])
            failure_status = eval(table_config['status_failure'])
            unprocessed_status = eval(table_config['status_unprocessed'])
            cursor = conn.cursor()
            parameters = []
            if unprocessed_status == None:
                get_unprocessed_rows = "select * from %s where %s is NULL" % (table_select, status_column)
            else:
                get_unprocessed_rows = "select * from %s where %s = ?" % (table_select, status_column)
                parameters.append(unprocessed_status)
            cursor.execute("select * from %s" %(table_select))
            logging.info("executing %s" % (get_unprocessed_rows))
            cursor.execute(get_unprocessed_rows, parameters)
            headers = map(lambda desc: desc[0].lower(), cursor.description)
            in_directory = integration_config['csv_uri'] + '/' + entity + '/in/'
            filename = "_" + entity + '_' + datetime.datetime.now().strftime("%Y-%m-%dT%H%M%S") + '.csv'
            txn_ids = []
            cursor_rows = cursor.fetchall()
            logging.info(str(len(cursor_rows)) + ' rows found')
            if len(cursor_rows) > 0 :
                with open(in_directory + filename, 'w') as outfile:
                    csvwriter = csv.writer(outfile)
                    csvwriter.writerow(mapper.keys())

                    for cursor_row in cursor_rows:
                        row = {}
                        map(lambda cursor_row_item: row.update({cursor_row_item[0]: cursor_row_item[1]}),
                                zip(headers, cursor_row))

                        csv_row = []
                        for csvheader in mapper:
                            csv_row.append(eval(mapper[csvheader]))
                        encoded_csv_row = encode_row(csv_row)
                        csvwriter.writerow(encoded_csv_row)
                        if txn_id_column:
                            txn_ids.append(row[sanitize_object_name(txn_id_column)])

                        cursor.close()
            if(os.path.exists(os.path.join(in_directory, filename))):
                os.rename(os.path.join(in_directory, filename), os.path.join(in_directory, filename[1:]))
            cursor = conn.cursor()

            if table_config['status_column'] and table_config['txn_id_column'] and txn_ids:
                logging.info("updating status to %s for txn ids : %s" % (eval(table_config['status_inprogress']),', '.join(map(str, txn_ids))))
                if 'status_logging_column' in table_config.keys():
                     cursor.execute("update %s set %s = ?, %s = ? where %s in (%s)" % (
                        table_name, table_config['status_column'], table_config['status_logging_column'], txn_id_column, ', '.join(map(str, txn_ids))),
                                   (eval(table_config['status_inprogress']), JOB_TIME))
                else:
                    cursor.execute("update %s set %s = ? where %s in (%s)" % (
                        table_name, table_config['status_column'], txn_id_column, ', '.join(map(str, txn_ids))),
                                   (eval(table_config['status_inprogress'])))
            logging.info("done import %s" % (entity))
            cursor.close()

            # propogating result back to staging table
            if (txn_id_column):
                cursor = conn.cursor()
                out_directory = integration_config['csv_uri'] + '/' + entity + '/out/'
                csv_regex = re.compile("%s_\d{4}-\d{2}-\d{2}T\d{6}\.csv" % (entity))
                for csvfilename in os.listdir(out_directory):
                    if not re.match(csv_regex, csvfilename):
                        continue
                    csvfilepath = os.path.join(out_directory, csvfilename)
                    logging.info("propogating status for " + csvfilepath)
                    with open(csvfilepath, 'r') as csvfile:
                        csvreader = csv.DictReader(csvfile)
                        for csvrow in csvreader:
                            txn_ids.append(csvrow[sanitize_object_name(txn_id_column)])
                            if table_config['status_column'] and 'status_logging_column' in table_config.keys():
                                logging.info("updating status to %s for processed txn id : %s" % ((success_status if csvrow[FEEDBACK_STATUS_COLUMN].lower() == 'true' else failure_status),csvrow[sanitize_object_name(txn_id_column)]))
                                cursor.execute("update %s set %s = ?, %s = ?, %s = ? where %s = ?"
                                               % (table_name, table_config['status_column'], table_config['status_logging_column'],
                                                  table_config['status_description_column'], txn_id_column),
                                               (success_status if csvrow[
                                                                      FEEDBACK_STATUS_COLUMN].lower() == 'true' else failure_status,
                                                JOB_TIME,csvrow[FEEDBACK_DESC_COLUMN], csvrow[sanitize_object_name(txn_id_column)]))
                            elif table_config['status_column']:
                                logging.info("updating status to %s for processed txn id : %s" % ((success_status if csvrow[FEEDBACK_STATUS_COLUMN].lower() == 'true' else failure_status),csvrow[sanitize_object_name(txn_id_column)]))
                                cursor.execute("update %s set %s = ?, %s = ? where %s = ?"
                                               % (table_name, table_config['status_column'],
                                                  table_config['status_description_column'], txn_id_column),
                                               (success_status if csvrow[
                                                                      FEEDBACK_STATUS_COLUMN].lower() == 'true' else failure_status,
                                                csvrow[FEEDBACK_DESC_COLUMN], csvrow[sanitize_object_name(txn_id_column)]))
                    os.rename(out_directory + csvfilename, out_directory + PROCESSED_FILE_PREFIX + csvfilename)
                    logging.info("done propogation")

                cursor.close()

        conn.close()
    except Exception as ex:
        logging.exception(ex)
    finally:
        if conn:
            try:
                conn.close()
            except Exception:
                pass
    return


def handle_db_insta2x(dbconfig, entities, integration_config):
    conn = None
    try:
        conn = jaydebeapi.connect(dbconfig['driver_class'],
                                  dbconfig['jdbc_uri'],
                                  [dbconfig['db_username'], dbconfig['db_password']],
                                  dbconfig['classpath'])
        conn.jconn.setAutoCommit(False)
        for entity in entities:
            logging.info("exporting entity %s" % (entity))
            table_config = entities[entity]['x_type_config']
            mapper = entities[entity]['mapper']
            table_name = table_config['table']

            out_directory = integration_config['csv_uri'] + '/' + entity + '/out/'
            done_directory = integration_config['csv_uri'] + '/' + entity + '/done/'

            if not os.path.exists(out_directory) or not os.path.isdir(out_directory):
                logging.error("Out directory does not exist. Skipping " + entity)
                continue

            if not os.path.exists(done_directory) or not os.path.isdir(done_directory):
                logging.info('Creating done dir')
                os.makedirs(done_directory)


            csv_regex = re.compile("%s_\d{4}-\d{2}-\d{2}T\d{6}\.csv" % (entity));
            cursor = conn.cursor()
            for csvfilename in os.listdir(out_directory):
                if not re.match(csv_regex, csvfilename):
                    continue
                csvfilepath = os.path.join(out_directory, csvfilename)
                logging.info('Reading ' + csvfilepath)

                with open(csvfilepath, 'r') as csvfile:
                    tryLock(csvfile)
                    csvreader = csv.DictReader(csvfile)
                    for row in csvreader:

                        values = []
                        for column in mapper:
                            value = eval(mapper[column])
                            values.append(value)
            
                        insert_statement = 'insert into %s (%s) values(%s)' % (
                            table_name, ','.join(mapper.keys()),','.join(['?'] * len(values)))
                        cursor.execute(insert_statement, decode_row(values))
                    conn.commit()
                    unlock(csvfile)
                    logging.info('exported %s' % (csvfilename))
                os.rename(csvfilepath, os.path.join(done_directory, csvfilename))
            cursor.close()
            logging.info("done exporting %s" % (entity))

    except Exception as ex:
        logging.exception(ex)
        if conn:
            try:
                conn.rollback()
            except Exception:
                pass
    finally:
        if conn:
            try:
                conn.close()
            except Exception:
                pass


if (len(sys.argv) != 3):
    print("Usage: csv-integration <integration-config-file.json> <log_suffix>")
    exit(1)

if (not os.path.exists(sys.argv[1])):
    print("Config path does not exist.")
    exit(1)
integration_config = json.loads(open(sys.argv[1]).read())
log_suffix = sys.argv[2]

logging_directory = os.environ.get('INSTA_LOGPATH', '/var/log/insta')
if not os.path.exists(logging_directory):
    os.makedirs(logging_directory)

logging.basicConfig(format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s',
                    filename=logging_directory + '/csv-integration_' + log_suffix + '.log',
                    level=logging.DEBUG, datefmt='%d/%m/%Y %I:%M:%S %p')

logging.info('started')

for integration in integration_config['integrations']:
    if (integration['x_type'] == 'db'):
        if (integration['type'] == 'x2insta'):
            handle_db_x2insta(integration['x_type_config'], integration['entities'], integration)
        elif integration['type'] == 'insta2x':
            handle_db_insta2x(integration['x_type_config'], integration['entities'], integration)

    else:
        logging.debug("x_type %s not supported" % (integration['x_type']))
