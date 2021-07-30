-- liquibase formatted sql
-- changeset adityabhatia02:minio-migration-table

alter table patient_documents add is_migrated char(1);
update patient_documents set is_migrated = '0';
alter table patient_documents alter is_migrated set default '0';

create table minio_patient_documents (
id SERIAL PRIMARY KEY,
path character varying(1000) NOT NULL,
doc_id integer NOT NULL
);

alter table only minio_patient_documents
    ADD constraint doc_id_fkey foreign key (doc_id) references patient_documents(doc_id);

SELECT comment_on_table_or_sequence_if_exists('minio_patient_documents', true, 'Txn', 'All documents stored in minio/S3 linked against a Patient');
SELECT comment_on_table_or_sequence_if_exists('minio_patient_documents_id_seq', false, 'Txn', 'Sequence for id column of minio_patient_documents');

