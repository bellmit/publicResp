-- liquibase formatted sql
-- changeset adeshatole:add-pk-to-package-issue-uom-2 failOnError:false

ALTER TABLE package_issue_uom  ADD UNIQUE(package_uom,issue_uom);
ALTER TABLE package_issue_uom ALTER COLUMN package_uom SET NOT NULL;
ALTER TABLE package_issue_uom ALTER COLUMN issue_uom SET NOT NULL;