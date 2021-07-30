-- liquibase formatted sql
-- changeset adityabhatia02:add-short-code-for-confidentiality

ALTER TABLE confidentiality_grp_master ADD COLUMN abbreviation varchar(4);
UPDATE confidentiality_grp_master set abbreviation = 'GEN' where id = 0;
ALTER TABLE confidentiality_grp_master ADD COLUMN emr_access boolean DEFAULT true;
