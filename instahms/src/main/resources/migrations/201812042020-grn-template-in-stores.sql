-- liquibase formatted sql
-- changeset adeshatole:grn-template-dropdown-in-stores-master

ALTER TABLE stores ADD COLUMN grn_print_template character varying NOT NULL default 'BUILTIN_HTML';
UPDATE stores SET grn_print_template = (select grn_print_template from generic_preferences);