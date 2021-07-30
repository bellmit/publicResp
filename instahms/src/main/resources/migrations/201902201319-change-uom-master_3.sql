-- liquibase formatted sql
-- changeset adeshatole:add-pk-to-package-issue-uom-3 failOnError:false

CREATE SEQUENCE package_issue_uom_seq;
ALTER TABLE package_issue_uom ADD COLUMN uom_id integer PRIMARY KEY DEFAULT nextval('package_issue_uom_seq');