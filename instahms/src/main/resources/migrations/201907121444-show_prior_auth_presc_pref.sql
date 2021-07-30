-- liquibase formatted sql
-- changeset shilpanr:adding-new-column-prior-auth-prescription-in-generic-pref

ALTER TABLE generic_preferences ADD COLUMN show_prior_auth_presc CHARACTER VARYING(1) DEFAULT 'A';
