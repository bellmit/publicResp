-- liquibase formatted sql
-- changeset anandpatel:setting-column-priority-in-item_insurance_categories-table-sequential-default-value
UPDATE item_insurance_categories iic SET priority = sub.priority 
FROM (SELECT insurance_category_id,row_number() OVER (ORDER BY insurance_category_id) AS priority FROM item_insurance_categories) sub 
WHERE iic.insurance_category_id =sub.insurance_category_id;
