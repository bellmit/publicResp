-- liquibase formatted sql
-- changeset manasaparam:adding-generic-doc-TYPE

INSERT INTO doc_type VALUES ((SELECT concat(prefix, pattern,nextval('doc_type_seq')) FROM  unique_number WHERE type_number = 'doc_type'),'GENERAL DOCUMENT','N','GEN','A');
UPDATE patient_documents SET doc_type =(SELECT doc_type_id FROM doc_type WHERE doc_type_name='GENERAL DOCUMENT') WHERE (doc_type='' AND doc_format ='doc_fileupload');