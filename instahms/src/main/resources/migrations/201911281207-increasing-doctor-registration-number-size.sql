-- liquibase formatted sql
-- changeset manasaparam:increasing doctor registration number filed size to 25

alter table doctors alter column registration_no TYPE VARCHAR(25);
