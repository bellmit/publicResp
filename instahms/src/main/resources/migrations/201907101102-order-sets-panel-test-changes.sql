-- liquibase formatted sql
-- changeset mancini2802:order-set-changes-for-panel-tests

INSERT INTO package_category_master(package_category_id, package_category, status) VALUES (-2, 'Panel Test', 'A');

ALTER TABLE packages add column package_category_id INTEGER DEFAULT(-1);