-- liquibase formatted sql
-- changeset rajendratalekar:add-reg-perf-no-reg-charge-sources

ALTER TABLE registration_preferences ADD COLUMN no_reg_charge_sources VARCHAR(300) DEFAULT '';

