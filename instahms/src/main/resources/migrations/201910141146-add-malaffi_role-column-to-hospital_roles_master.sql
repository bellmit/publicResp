-- liquibase formatted sql
-- changeset prashantbaisla:add-malaffi_role-column-to-hospital_roles_master

ALTER TABLE hospital_roles_master ADD COLUMN malaffi_role character(1);

COMMENT ON COLUMN hospital_roles_master.malaffi_role IS 'P - Primary Provider, S - Secondary Provider, T - Tertiary Provider, F - Front Desk, NULL - Not set/Unset';
