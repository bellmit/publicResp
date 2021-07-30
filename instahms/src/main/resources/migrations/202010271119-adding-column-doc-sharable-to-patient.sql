-- liquibase formatted sql
-- changeset raeshmika:<column-doc-sharable-to-patient>

ALTER TABLE doc_type ADD COLUMN isShareableToPatient CHARACTER(1) DEFAULT 'N';
UPDATE doc_type set isShareableToPatient = 'N' where system_type = 'N';
UPDATE doc_type set isShareableToPatient = 'Y' where doc_type_id in ('SYS_LR', 'SYS_DS', 'SYS_RR', 'SYS_SS', 'SYS_RX', 'SYS_ST', 'DC0004', 'SYS_CONSULT','SYS_GROWTH_CHART');