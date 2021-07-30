-- liquibase formatted sql
-- changeset asif:indexing-on_insurance_plan_status_category failOnError:false
-- validCheckSum: ANY

create index insurance_plan_status_category_idx on insurance_plan_main(status, category_id);
