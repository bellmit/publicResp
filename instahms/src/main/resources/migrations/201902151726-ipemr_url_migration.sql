-- liquibase formatted sql
-- changeset sonam009:ipemr-url-right-migration

INSERT INTO url_action_rights (SELECT DISTINCT role_id, 'new_ipemr', 'A' FROM url_action_rights WHERE action_id IN('visit_summary','ip_prescriptions','doctors_note','nurse_note','patient_summary') AND rights='A');
