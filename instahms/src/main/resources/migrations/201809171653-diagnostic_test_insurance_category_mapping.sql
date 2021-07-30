-- liquibase formatted sql
-- changeset tejasiitb:create-table-diagnostic-test-insurance-category-mapping
CREATE TABLE diagnostic_test_insurance_category_mapping (
	diagnostic_test_id character varying(50) NOT NULL,
	insurance_category_id integer NOT NULL
);
CREATE INDEX diagnostic_test_id_diagnostic_test_insurance_category_mapping  ON 
diagnostic_test_insurance_category_mapping USING btree(diagnostic_test_id);
CREATE INDEX insurance_category_id_diagnostic_test_insurance_category_mapping  ON 
diagnostic_test_insurance_category_mapping USING btree(insurance_category_id);
INSERT INTO diagnostic_test_insurance_category_mapping 
SELECT test_id,insurance_category_id FROM diagnostics;