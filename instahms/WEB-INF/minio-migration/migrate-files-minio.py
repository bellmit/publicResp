#!/usr/bin/python

from minio import Minio, sse
from minio.error import (
    ResponseError, BucketAlreadyOwnedByYou, BucketAlreadyExists
)
from utils import (
    read_minio_config_from_environment_properties, get_db_connection,
    execute_query, set_search_path, get_all_databases, get_all_schemas
)
import psycopg2
import logging
import time
import hashlib
import sys
import io
import base64

limit = 5000
logging.basicConfig(
    format='%(pathname)s:%(lineno)d %(asctime)s-%(levelname)s:%(message)s',
    filename='/var/log/insta/minio_migrate.log',
    level=logging.DEBUG,
    datefmt='%d/%m/%Y %I:%M:%S %p'
)

minio_config = read_minio_config_from_environment_properties()
bucket_name = minio_config.pop('bucket_name')
try:
    bucket_region = minio_config.pop('region')
except KeyError:
    bucket_region = None
# Initialize minioClient with an endpoint and access/secret keys.
minioClient = Minio(**minio_config)

query_get_docs = """SELECT doc_id,doc_content_bytea,content_type from patient_documents
                        where is_migrated = (%s) AND doc_content_bytea IS NOT NULL limit (%s);"""
query_set_is_migrated_true = """UPDATE patient_documents set is_migrated = (%s)
                                where doc_id = (%s)"""
query_add_path_minio_table = """INSERT into minio_patient_documents (path, doc_id)
                                    VALUES ((%s), (%s))"""
query_get_minio_sse_key = "SELECT sse_key FROM minio_sse"

try:
    logging.info("Attempting to create bucket:" + bucket_name)
    if bucket_region is None:
        minioClient.make_bucket(bucket_name)
    else:
        minioClient.make_bucket(bucket_name, location=bucket_region)
    logging.info("Created bucket named:" + bucket_name)
except BucketAlreadyOwnedByYou as err:
    logging.info("Bucket owned by you")
except BucketAlreadyExists as err:
    logging.info("BucketAlreadyExists")
except ResponseError as err:
    logger.error('Unable to connect to minio while creating bucket : ' + bucket_name)
    raise

logging.info("Connected to minio")
for dbname in get_all_databases():
    for schema in get_all_schemas(dbname):
        db_connection = get_db_connection(dbname)
        cur = set_search_path(db_connection.cursor(), schema)
        cur.execute(query_get_minio_sse_key)
        row = cur.fetchone()
        sse_object = sse.SSE_C(base64.b64decode(row[0]));
        try:
            cur.execute(query_get_docs, ["0", limit])
            for record in cur:
                doc_id = str(record[0])
                logging.info("Attempting to migrate doc with id:" + doc_id)
                bytes_io = io.BytesIO()
                data = record[1]
                bytes_io.write(data)
                bytes_io.seek(0)
                content_type = record[2]
                logging.info("content type:" + content_type)
                extension = content_type.split('/')[1]
                logging.info("extension:" + extension)
                md5_doc_object = hashlib.md5(data).hexdigest()
                try:
                    path = schema + "/" + doc_id + "_" + str(time.time()) + "." + extension
                    minioClient.put_object(
                        bucket_name, path, bytes_io, len(data),
                        content_type=content_type, sse=sse_object
                    )
                    #get object and verify md5
                    retrieved_obj = minioClient.get_object(bucket_name, path, sse=sse_object)
                    retrieved_data = retrieved_obj.read()
                    if md5_doc_object != hashlib.md5(retrieved_data).hexdigest():
                        logging.error("md5sum DO NOT match for doc_id:" + doc_id)
                        continue
                    logging.info("Able to put object with doc_id:" + doc_id)
                    #set column is migrated as true
                    execute_query(query_set_is_migrated_true, ["1", doc_id], dbname, schema)
                    #add entry to table with the new path
                    execute_query(query_add_path_minio_table, [path, doc_id], dbname, schema)
                except ResponseError:
                    #log exception and failure message
                    logging.exception("FAILED to put object with doc_id:" + doc_id)
        except Exception:
            logging.exception("Exiting due to fatal exception")
            exit()

