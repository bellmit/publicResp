-- liquibase formatted sql
-- changeset sanjana.goyal:genders

INSERT INTO gender_master(gender_name) values ('Female');
INSERT INTO gender_master(gender_name) values ('Male');
INSERT INTO gender_master(gender_name) values ('Other');
INSERT INTO gender_master(gender_name) values ('Unknown');
INSERT INTO gender_master(gender_name) values ('Ambiguous');
INSERT INTO gender_master(gender_name) values ('Not Applicable');
