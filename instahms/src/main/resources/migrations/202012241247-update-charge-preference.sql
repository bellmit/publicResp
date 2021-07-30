-- liquibase formatted sql
-- changeset anandpracto:adding-column-update_charge_on_rate_plan_change-in-generic-preference
ALTER TABLE generic_preferences ADD COLUMN update_charge_on_rate_plan_change VARCHAR(1) DEFAULT 'Y';
