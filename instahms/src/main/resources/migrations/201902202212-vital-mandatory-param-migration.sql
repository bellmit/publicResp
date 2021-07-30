-- liquibase formatted sql
-- changeset janakivg:vital-mandatory-param-migration

update vitals_default_details set mandatory ='Y'  where param_id IN (select param_id from vital_parameter_master where mandatory_in_tx ='Y');
