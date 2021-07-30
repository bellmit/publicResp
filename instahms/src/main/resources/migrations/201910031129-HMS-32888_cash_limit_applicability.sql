-- liquibase formatted sql
-- changeset manjular:adding-new-column-cash-limit-applicability-in-generic-pref

ALTER TABLE generic_preferences ADD COLUMN income_tax_cash_limit_applicability CHARACTER VARYING(1) DEFAULT 'N'::bpchar;

