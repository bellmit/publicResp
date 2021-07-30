-- liquibase formatted sql
-- changeset vakul-practo:add-doescaping-column-to-interfaces splitStatements:false
-- validCheckSum: ANY

ALTER TABLE interface_config_master ADD COLUMN do_escaping VARCHAR(1) DEFAULT 'Y';

