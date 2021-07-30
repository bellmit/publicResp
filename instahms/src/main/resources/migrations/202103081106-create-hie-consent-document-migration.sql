-- liquibase formatted sql
-- changeset asif:<creating-HIE-consent-document-migration>

INSERT INTO doc_type (doc_type_id, doc_type_name, system_type, prefix, status) VALUES ('SYS_HIE', 'HIE Consent', 'Y', 'HIE', 'A');
INSERT INTO doc_category_master (doc_category_id, doc_category_name) values ('HIE', 'HIE Document');
INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_HIE', 'HIE');
