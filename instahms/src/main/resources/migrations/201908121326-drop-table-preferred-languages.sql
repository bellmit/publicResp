-- liquibase formatted sql
-- changeset raj-nt:drop-table-preferred-languages

ALTER TABLE contact_preferences DROP CONSTRAINT contact_preferences_lang_code_fkey;
ALTER TABLE generic_preferences DROP CONSTRAINT generic_preferences_contact_pref_lang_code_fkey;
DROP TABLE preferred_languages;
