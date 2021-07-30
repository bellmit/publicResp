-- liquibase formatted sql
-- changeset yashwantkumar:tests-prescribed-pres-date-idx

create index tests_prescribed_pres_date_idx on tests_prescribed (CAST(pres_date AS date));
