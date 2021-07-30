-- liquibase formatted sql
-- changeset adeshatole:add-pk-to-package-issue-uom
-- validCheckSum: ANY

-- this changeset is split across three files to handle missing primary key constraint on some schemas
ALTER TABLE package_issue_uom DROP CONSTRAINT IF EXISTS package_issue_uom_primarykey;