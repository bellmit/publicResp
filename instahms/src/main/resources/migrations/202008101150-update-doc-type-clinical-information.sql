-- liquibase formatted sql
-- changeset pranays:update-doc-type-clinical-information-hms-36327

UPDATE doc_type SET doc_type_name='Dialysis Clinical Document' WHERE doc_type_id='SYS_CLINICAL';
