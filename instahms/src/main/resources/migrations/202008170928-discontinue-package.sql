-- liquibase formatted sql
-- changeset krishnasameerachanta:<add-discontinue-package-columns>

ALTER TABLE  patient_customised_package_details ADD COLUMN is_discontinued boolean;
ALTER TABLE patient_customised_package_details ADD COLUMN discontinue_remark character varying(4000);

insert into action_rights(action,role_id,rights) select 'multi_visit_package_discontinuation', role_id,'N' from u_role;
