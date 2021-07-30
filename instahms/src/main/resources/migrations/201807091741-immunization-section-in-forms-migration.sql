-- liquibase formatted sql
-- changeset tejakilaru:<immunization-section-in-forms-migration>

update form_components SET sections = '-17,' || sections where immunization='Y';
