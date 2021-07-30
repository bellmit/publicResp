-- liquibase formatted sql
-- changeset vishwas07:append_weeks_to_old_data

update antenatal set gestation_age = gestation_age||' Weeks' where gestation_age not like '%Weeks%';
