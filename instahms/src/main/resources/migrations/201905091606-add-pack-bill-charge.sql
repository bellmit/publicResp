-- liquibase formatted sql

-- changeset harishm18:adding package id and panel id in bill charge

ALTER TABLE bill_charge ADD COLUMN package_id integer;
ALTER TABLE bill_charge ADD COLUMN panel_id integer;