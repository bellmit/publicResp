-- liquibase formatted sql
-- changeset tejasiitb:adding package id in store_issue_main

ALTER TABLE stock_issue_main ADD COLUMN package_id integer;
ALTER TABLE store_issue_returns_main ADD COLUMN package_id integer;
