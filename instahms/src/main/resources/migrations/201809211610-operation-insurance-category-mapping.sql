-- liquibase formatted sql
-- changeset tejasiitb:create-table-operation-insurance-category-mapping

CREATE TABLE operation_insurance_category_mapping
(
   operation_id     varchar(10)   NOT NULL,
   insurance_category_id  integer       NOT NULL
);

CREATE INDEX operation_id_operation_insurance_category_mapping ON 
operation_insurance_category_mapping USING btree (operation_id);
CREATE INDEX insurance_category_id_operation_insurance_category_mapping ON 
operation_insurance_category_mapping USING btree (insurance_category_id);

INSERT INTO operation_insurance_category_mapping 
SELECT op_id,insurance_category_id FROM operation_master;