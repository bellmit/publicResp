-- liquibase formatted sql
-- changeset vishwas07:package id in scheduler appointment to show package info

ALTER TABLE scheduler_appointments ADD COLUMN package_id integer;

ALTER TABLE scheduler_appointments ADD COLUMN parent_pack_ob_id INTEGER;

ALTER TABLE scheduler_appointments ADD FOREIGN KEY(package_id) REFERENCES packages(package_id);