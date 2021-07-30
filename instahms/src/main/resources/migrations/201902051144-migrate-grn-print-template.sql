-- liquibase formatted sql
-- changeset adeshatole:migrate-default-grn-print-template

UPDATE stores SET grn_print_template = (select obsolete_default_grn_print_template from generic_preferences);