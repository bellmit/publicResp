-- liquibase formatted sql
-- changeset raeshmika:<migration-and-mapping-of-doc-types-to-category>

-- pre shipped document categories --

INSERT INTO doc_category_master (doc_category_id, doc_category_name) values ('CLN', 'Clinical');

INSERT INTO doc_category_master (doc_category_id, doc_category_name) values ('REGI', 'Registration');

INSERT INTO doc_category_master (doc_category_id, doc_category_name) values ('INS', 'Insurance');

--  migrate system defined doc types to a catgeory --

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES (1, 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES (5, 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_LR', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_DS', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_CI', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_CI', 'INS');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_RR', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_SS', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_RX', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_VP', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES (4, 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_ST', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_RG', 'REGI');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_INS', 'INS');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_TPA', 'INS');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_DIE', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_OP', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_OT', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_DIALYSIS', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_MRDCODE', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_MRDCODE', 'INS');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_CONSULT', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_CLINICAL', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_TRIAGE', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_ASSESSMENT', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_PROGRESS_NOTES', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_CLINICAL_LAB', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_IP', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_IVP', 'CLN');

INSERT INTO doc_type_category_mapping(doc_type_id, doc_type_category_id) VALUES ('SYS_GROWTH_CHART', 'CLN');
