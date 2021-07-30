-- liquibase formatted sql
-- changeset sanjana.goyal:action-right-for-mark-patient-duplicate


insert into action_rights(action,role_id,rights) select 'mark_patient_duplicate', role_id,'A' from u_role;