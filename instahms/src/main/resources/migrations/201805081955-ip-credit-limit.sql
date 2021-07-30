-- liquibase formatted sql
-- changeset satishl2772:adding-all-ip-credit-limit-db-queries

ALTER TABLE generic_preferences ADD COLUMN ip_credit_limit_rule character(1) NOT NULL DEFAULT 'A';
ALTER TABLE patient_registration ADD COLUMN ip_credit_limit_amount numeric(15,2);
INSERT INTO action_rights (SELECT role_id, 'allow_ip_patient_credit_limit_change', 'N' FROM u_role);
