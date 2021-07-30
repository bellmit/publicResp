-- liquibase formatted sql
-- changeset raeshmika:<add-new-column-specalized-docs-for-preshipped-values>


ALTER TABLE doc_category_master ADD COLUMN specialized character varying(1) DEFAULT 'N' NOT NULL;

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('mlc', 'mlc', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('service', 'service', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('reg', 'reg', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('insurance', 'insurance', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('dietary', 'dietary', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('tpapreauth', 'tpapreauth', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('op_case_form_template', 'op_case_form_template', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('ot', 'ot', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('plan_card', 'plan_card', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('corporate_card', 'corporate_card', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('national_card', 'national_card', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('lab_test_doc', 'lab_test_doc', 'Y');

INSERT INTO doc_category_master (doc_category_id, doc_category_name, specialized) values ('rad_test_doc', 'rad_test_doc', 'Y');
