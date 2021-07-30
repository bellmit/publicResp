-- liquibase formatted sql
-- changeset vishwas07:<commit-message-describing-this-database-change>

ALTER TABLE antenatal_main ADD COLUMN close_pregnancy CHAR(1) NOT NULL DEFAULT 'N';

ALTER TABLE antenatal_main ADD COLUMN close_pregnancy_user CHARACTER VARYING(100);

ALTER TABLE antenatal_main ADD COLUMN close_pregnancy_date_time timestamp without time zone;
