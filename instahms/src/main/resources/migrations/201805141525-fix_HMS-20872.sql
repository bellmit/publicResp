-- liquibase formatted sql
-- changeset SirishaRL:HMS-20872
ALTER TABLE stores ALTER COLUMN sale_unit SET DEFAULT 'I';
UPDATE stores SET sale_unit = 'I' where sale_unit='';
