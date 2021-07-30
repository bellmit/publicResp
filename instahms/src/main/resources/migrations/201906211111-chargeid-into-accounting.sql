-- liquibase formatted sql
-- changeset satishl2772:adding-chargeid-and-primarykey-into-accounting-table

ALTER TABLE hms_accounting_info ADD COLUMN charge_reference_id character varying(15);

COMMENT ON COLUMN hms_accounting_info.charge_reference_id IS 'It will holds the charge id';

ALTER TABLE hms_accounting_info ADD COLUMN primary_id character varying(15);

COMMENT ON COLUMN hms_accounting_info.primary_id IS 'It will holds the primary key value of charge from main or details tables';

ALTER TABLE hms_accounting_info ADD COLUMN secondary_id character varying(15);

COMMENT ON COLUMN hms_accounting_info.secondary_id IS 'It will holds the secondary key from composite primary key value of charge from main or details tables';

ALTER TABLE hms_accounting_info ADD COLUMN primary_id_reference_table character varying;

COMMENT ON COLUMN hms_accounting_info.primary_id_reference_table IS 'It will holds the hms_accounting_info.primary_id column data related table name';
