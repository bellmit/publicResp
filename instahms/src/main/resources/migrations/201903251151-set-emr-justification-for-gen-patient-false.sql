-- liquibase formatted sql
-- changeset adityabhatia02:dont-require-emr-comments-for-gen-patient

UPDATE confidentiality_grp_master SET emr_access = 'f' where confidentiality_grp_id = '0';