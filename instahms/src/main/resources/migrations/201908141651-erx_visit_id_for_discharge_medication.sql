-- liquibase formatted sql
-- changeset sonam009:add-column-erx-visit-id-for-discharge-medication-support

ALTER TABLE pbm_prescription ADD COLUMN erx_visit_id character varying(15);
