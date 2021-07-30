-- liquibase formatted sql
-- changeset mohamedanees:add-accumed-preference-to-center

ALTER TABLE center_preferences ADD COLUMN accumed_ftp_password character varying (100);
ALTER TABLE center_preferences ADD COLUMN accumed_ftp_username character varying (100);
ALTER TABLE center_preferences ADD COLUMN accumed_ftp_url character varying (1000);
