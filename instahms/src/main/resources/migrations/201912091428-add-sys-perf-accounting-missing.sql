-- liquibase formatted sql
-- changeset rajendratalekar:add-sys-perf-accounting-missing

ALTER TABLE generic_preferences 
	ADD COLUMN accounting_missing_data_scan_rel_start smallint DEFAULT 24, 
	ADD COLUMN accounting_missing_data_scan_rel_end smallint DEFAULT 2;

