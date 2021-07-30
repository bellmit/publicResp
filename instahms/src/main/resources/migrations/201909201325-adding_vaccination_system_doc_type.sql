-- liquibase formatted sql
-- changeset tejakilaru:adding_vaccination_system_doc_type

INSERT into doc_type VALUES ('SYS_VACCINATION', 'Vaccination Records', 'Y', 'VR', 'I');
