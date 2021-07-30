-- liquibase formatted sql
-- changeset adeshatole:deprecate-default-grn-print-template-from-generic-preferences

ALTER TABLE generic_preferences RENAME COLUMN default_grn_print_template TO obsolete_default_grn_print_template;