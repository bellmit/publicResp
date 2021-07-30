-- liquibase formatted sql
-- changeset sreenivasayashwanth:<drop-pref-appointment_name_order>

ALTER TABLE generic_preferences DROP COLUMN appointment_name_order;