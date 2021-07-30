-- liquibase formatted sql
-- changeset anandpatel:adding-new-table-insurance_company_category_mapping-for-mapping-insurance-comapny-and-insurance-item-category
CREATE TABLE insurance_company_category_mapping (
insurance_co_id character varying(10) NOT NULL,
insurance_category_id integer NOT NULL
);

INSERT INTO item_insurance_categories (insurance_category_id, insurance_category_name, insurance_payable, system_category) 
VALUES (0, 'Default', 'N', 'Y');

INSERT INTO insurance_company_category_mapping 
(SELECT insurance_co_id , insurance_category_id FROM insurance_company_master 
INNER JOIN item_insurance_categories on true);
