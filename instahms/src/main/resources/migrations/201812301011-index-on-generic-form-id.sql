-- liquibase formatted sql
-- changeset goutham005:index-on-generic-form-id

 CREATE INDEX patient_section_details_generic_form_id_idx ON patient_section_details (generic_form_id);
 