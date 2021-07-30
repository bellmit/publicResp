-- liquibase formatted sql
-- changeset utkarshjindal:<adding-unique-constraint-practitioner-name>
ALTER TABLE practitioner_types  ADD CONSTRAINT pt_practitioner_name UNIQUE(practitioner_name);
